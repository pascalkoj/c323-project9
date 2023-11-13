package com.example.project9

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.project9.databinding.FragmentCreateBinding
import com.example.project9.model.Post
import com.example.project9.model.User
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date




class CreateFragment : Fragment() {
    val TAG = "CreateFragment"
    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!
    private var signedInUser: User? = null
    private var photoUri: Uri? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        // Registers a photo picker activity launcher in single-select mode.
        val view = binding.root
        firestoreDb = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d(TAG, "Selected URI: $uri")
                photoUri = uri
                binding.imageView.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }
        binding.btnPickImage.setOnClickListener {
            Log.i(TAG, "Open up image picker on device")

            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSubmit.setOnClickListener {
            postThePhoto()
        }
        getTheCurrentUser()

        return view
    }


    private fun getTheCurrentUser() {
        val viewModel: PostsViewModel by activityViewModels()
        signedInUser = viewModel.signedInUser
        /*
        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure fetching signed in user", exception)
            }

         */
    }

    private fun postThePhoto() {
        if (photoUri == null) {
            Toast.makeText(this.requireContext(), "No photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etDescription.text.toString().isBlank()) {
            Toast.makeText(this.requireContext(), "Description cannot be empty", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (signedInUser == null) {
            Toast.makeText(
                this.requireContext(),
                "No signed in user, please wait",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        binding.btnSubmit.isEnabled = false
        val photoUploadUri = photoUri as Uri
        val photoReference =
            storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        // Upload photo to Firebase Storage
        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                // Retrieve image url of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                // Create a post object with the image URL and add that to the posts collection
                val post = Post(
                    binding.etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser
                )
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                binding.btnSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception during Firebase operations", postCreationTask.exception)
                    Toast.makeText(this.requireContext(), "Failed to save post", Toast.LENGTH_SHORT)
                        .show()
                }
                binding.etDescription.text.clear()
                binding.imageView.setImageResource(0)
                Toast.makeText(this.requireContext(), "Success!", Toast.LENGTH_SHORT).show()
            }
        navigateToPostsScreen()
    }

    private fun navigateToPostsScreen() {
        binding.root.findNavController().navigate(R.id.action_createFragment_to_postsFragment)
    }


}