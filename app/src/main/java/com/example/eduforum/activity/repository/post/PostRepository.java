package com.example.eduforum.activity.repository.post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import android.util.Log;

import com.example.eduforum.activity.model.post_manage.Category;
import com.example.eduforum.activity.model.post_manage.Post;
import com.example.eduforum.activity.model.subscription_manage.Subscription;
import com.example.eduforum.activity.util.FlagsList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostRepository {
    private static PostRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseStorage firebaseStorage;

    public PostRepository() {
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public static synchronized PostRepository getInstance() {
        if (instance == null) {
            instance = new PostRepository();
        }
        return instance;
    }

    // TODO: add post image to firebase storage

    public void addPost(Post post, IPostCallback callback) {
        db.collection("Community")
                .document(post.getCommunityID())
                .collection("Post")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        post.setPostID(documentReference.getId());
                        callback.onAddPostSuccess(post);
                        Log.d(FlagsList.DEBUG_POST_FLAG, "New post written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onAddPostFailure(e.toString());
                        Log.w(FlagsList.DEBUG_POST_FLAG, "Error adding post", e);
                    }
                });
    }

    public void editPost(Post post, IPostCallback callback) {
        db.collection("Community")
                .document(post.getCommunityID())
                .collection("Post")
                .document(post.getPostID())
                .set(post, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onEditPostSuccess();
                        Log.d(FlagsList.DEBUG_POST_FLAG, "Post successfully edited!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onEditPostFailure(e.toString());
                        Log.w(FlagsList.DEBUG_POST_FLAG, "Error adding document", e);
                    }
                });
    }


    public void getPosts(String communityID, IPostCallback callback) {
        db.collection("Community")
                .document(communityID)
                .collection("Post")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Post> posts = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Post post = documentSnapshot.toObject(Post.class);
                            post.setPostID(documentSnapshot.getId());
                            posts.add(post);
                            Log.d(FlagsList.DEBUG_POST_FLAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                        }

                        callback.onGetPostSuccess(posts);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onGetPostFailure(e.toString());
                        Log.w(FlagsList.DEBUG_POST_FLAG, "Error fetching post,", e);
                    }
                });
    }

    //TODO: check if the user is the owner of the post, render a button can delete that post @Duong Thuan Tri
    // TODO: delete all comment subcollection when user delete a post
    public void deletePost(Post post, IPostCallback callback) {
        db.collection("Community")
                .document(post.getCommunityID())
                .collection("Post")
                .document(post.getPostID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onDeletePostSuccess();
                        Log.d(FlagsList.DEBUG_POST_FLAG, "Post successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onDeletePostError(e.toString());
                        Log.w(FlagsList.DEBUG_POST_FLAG, "Error deleting document", e);
                    }
                });
    }

    public void queryPost(String communityID, @Nullable List<Category> categories, @Nullable PostQuery condition, IPostCallback callback) {
        if (categories == null && condition == null) {
            callback.onQueryPostError("NO_CONDITION");
            return;
        }

        List<Post> queryPostResults = new ArrayList<>();

        CollectionReference postRef = db.collection("Community").document(communityID).collection("Post");
        Query postQuery = postRef;
        if (condition != null) {
            if (condition.isMostCommented()) {
                postQuery = postRef.orderBy("totalComment", Query.Direction.DESCENDING);
            }
            if (condition.isMostVoted()) {
                postQuery = postRef.orderBy("voteDifference", Query.Direction.DESCENDING);
            }
            if (condition.isNewest()) {
                postQuery = postRef.orderBy("timeCreated", Query.Direction.DESCENDING);
            }
            if (condition.isOldest()) {
                postQuery = postRef.orderBy("timeCreated", Query.Direction.ASCENDING);
            }
        }

        if (categories != null) {
            List<String> categoryIDs = new ArrayList<>();
            for (Category category: categories) {
                categoryIDs.add(category.getCategoryID());
            }

            List<List<String>> batches = new ArrayList<>();
            int batchSize = 10;
            for (int i = 0; i < categoryIDs.size(); i += batchSize) {
                int end = Math.min(categoryIDs.size(), i + batchSize);
                batches.add(new ArrayList<>(categoryIDs.subList(i, end)));
            }

            List<Task<QuerySnapshot>> tasks = new ArrayList<>();
            for (List<String> batch : batches) {
                Task<QuerySnapshot> task = postQuery.whereArrayContainsAny("categories.categoryID", batch).get();
                tasks.add(task);
            }

            Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                for (Object result : results) {
                    QuerySnapshot snapshot = (QuerySnapshot) result;
                    for (QueryDocumentSnapshot document : snapshot) {
                        queryPostResults.add(document.toObject(Post.class));
                        Log.d(FlagsList.DEBUG_POST_FLAG, document.getId() + " => " + document.getData());
                    }
                }
                callback.onQueryPostSuccess(queryPostResults);
            }).addOnFailureListener(e -> {
                callback.onQueryPostError(e.toString());
                Log.w(FlagsList.DEBUG_POST_FLAG, "Error getting documents", e);
            });

        } else {
            postQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            queryPostResults.add(document.toObject(Post.class));
                            Log.d(FlagsList.DEBUG_POST_FLAG, document.getId() + " => " + document.getData());
                        }
                        callback.onQueryPostSuccess(queryPostResults);
                    } else {
                        callback.onQueryPostError(Objects.requireNonNull(task.getException()).getMessage());
                        Log.d(FlagsList.DEBUG_POST_FLAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }

    }

    public void bookmarkPost(Post post, String userID, String communityName, IPostCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("userID", userID);
        Map<String, Object> communityObject = new HashMap<>();
        communityObject.put("communityID", post.getCommunityID());
        communityObject.put("name", communityName);
        data.put("community", communityObject);
        data.put("creator", post.getCreator());
        // put the whole post object just for testing purpose, this need to be optimized in the future
        data.put("post", post);

        db.collection("Bookmark")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        callback.onBookmarkSuccess();
                        Log.d(FlagsList.DEBUG_POST_FLAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onBookmarkError(e.toString());
                        Log.w(FlagsList.DEBUG_POST_FLAG, "Error adding document", e);
                    }
                });

    }

    public void subscribePost(String communityID, String postID, String userID, IPostCallback callback) {
        db.collection("Subscription")
                .add(new Subscription(communityID,postID,userID))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        callback.onSubscriptionSuccess();
                        Log.d(FlagsList.DEBUG_POST_FLAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onSubscriptionError(e.toString());
                        Log.w(FlagsList.DEBUG_POST_FLAG, "Error adding document", e);
                    }
                });
    }

    //TODO: transaction for upVote, downVote
    public void updateVoteCount(String communityID, String postID, int voteChange) {
        DocumentReference postRef = db.collection("Community")
                .document(communityID)
                .collection("Post")
                .document(postID);

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(postRef);

                // Assuming the post document has a "votes" field
                long newVoteCount = snapshot.getLong("votes") + voteChange;
                transaction.update(postRef, "votes", newVoteCount);

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Transaction", "Transaction success!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Transaction", "Transaction failure.", e);
            }
        });

        //how to call this function in FE @Duong Thuan Tri
        /*
            upvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postRepository.updateVoteCount(communityID, postID, 1);
                }
            });

            downvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postRepository.updateVoteCount(communityID, postID, -1);
                }
            });
        */
    }
}