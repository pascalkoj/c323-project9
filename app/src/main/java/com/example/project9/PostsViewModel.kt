package com.example.project9

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import com.example.project9.model.Post
import com.example.project9.model.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PostsViewModel : ViewModel() {


    val TAG = "PostsViewModel"
    var signedInUser: User? = null
    private val _posts: MutableLiveData<MutableList<Post>> = MutableLiveData()
    val posts: LiveData<List<Post>>
        get() = _posts as LiveData<List<Post>>

    init {

        val  firestoreDB = FirebaseFirestore.getInstance()
//        firestoreDB.collection("users")
//            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
//            .get()
//            .addOnSuccessListener { userSnapshot ->
//                signedInUser = userSnapshot.toObject<User>()
//                Log.i(TAG, "signed in user: $signedInUser")
//            }
//            .addOnFailureListener { exception ->
//                Log.i(TAG, "Failure fetching signed in user", exception)
//            }
        var postsReference = firestoreDB
            .collection("posts")
            .limit(30)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects<Post>()
            _posts.value = postList as MutableList<Post>
            for (post in postList) {
                Log.i(TAG, "Post ${post}")
            }
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        signedInUser = null
    }



    fun AddNewPicture(photoUri: Uri)
    {
        val firestoreDb = FirebaseFirestore.getInstance()
        val storageReference = FirebaseStorage.getInstance().reference
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
                    "",
                    downloadUrlTask.result.toString(), // img url
                    System.currentTimeMillis(),
                    signedInUser
                )
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception during Firebase operations", postCreationTask.exception)
                }
                Log.i("INFO", "Successfully uploaded $photoUri to database")
            }
    }


}