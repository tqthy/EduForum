package com.example.eduforum.activity.repository.comment;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.eduforum.activity.model.post_manage.Comment;
import com.example.eduforum.activity.model.post_manage.Post;
import com.example.eduforum.activity.model.post_manage.PostingObject;
import com.example.eduforum.activity.util.FlagsList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class CommentRepository {
    private static CommentRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseStorage firebaseStorage;

    public CommentRepository() {
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public static synchronized CommentRepository getInstance() {
        if (instance == null) {
            instance = new CommentRepository();
        }
        return instance;
    }

    // create a comment
    public void createComment(PostingObject parent, Comment newComment, CommentCallback callback) {
        CollectionReference commentRef = db.collection("Community")
                .document(parent.getCommunityID())
                .collection("Post")
                .document(parent.getPostID())
                .collection("Comment");
        if (parent instanceof Comment) { // is a reply
            Comment parentComment = (Comment) parent;
            newComment.setReplyCommentID(parentComment.getCommentID());
        }
        newComment.setCommunityID(parent.getCommunityID());
        newComment.setPostID(parent.getPostID());
        commentRef.add(newComment).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        newComment.setCommentID(documentReference.getId());
                        callback.onCreateSuccess(newComment);
                        Log.d(FlagsList.DEBUG_COMMENT_FLAG, "Comment written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e.getMessage());
                        Log.w(FlagsList.DEBUG_COMMENT_FLAG, "Error adding document", e);
                    }
                });

    }

    // load top-level-comments
    public void loadTopLevelComments(Post post, CommentCallback callback) {
        Query commentQuery = db.collection("Community")
                .document(post.getCommunityID())
                .collection("Post")
                .document(post.getPostID())
                .collection("Comment")
                .whereEqualTo("replyCommentID", null);
        commentQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Comment> comments = queryDocumentSnapshots.toObjects(Comment.class);
            callback.onInitialLoadSuccess(comments);
        }).addOnFailureListener(e -> {
            callback.onFailure(e.getMessage());
            Log.w(FlagsList.DEBUG_COMMENT_FLAG, "Error getting documents.", e);
        });
    }

    // load replies of a comment
    public void loadReplies(Comment comment, CommentCallback callback) {
        Query commentQuery = db.collection("Community")
                .document(comment.getCommunityID())
                .collection("Post")
                .document(comment.getPostID())
                .collection("Comment")
                .whereEqualTo("replyCommentID", comment.getCommentID());
        commentQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Comment> comments = queryDocumentSnapshots.toObjects(Comment.class);
            callback.onLoadRepliesSuccess(comments);
        }).addOnFailureListener(e -> {
            callback.onFailure(e.getMessage());
            Log.w(FlagsList.DEBUG_COMMENT_FLAG, "Error getting documents.", e);
        });
    }

    // delete a comment
    public void deleteComment(Comment comment, CommentCallback callback) {
        db.collection("Community")
                .document(comment.getCommunityID())
                .collection("Post")
                .document(comment.getPostID())
                .collection("Comment")
                .document(comment.getCommentID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onDeleteSuccess();
                    Log.d(FlagsList.DEBUG_COMMENT_FLAG, "Comment successfully deleted!");
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.w(FlagsList.DEBUG_COMMENT_FLAG, "Error deleting document", e);
                });
    }

    // update a comment
    public void updateComment(Comment comment, CommentCallback callback) {
        db.collection("Community")
                .document(comment.getCommunityID())
                .collection("Post")
                .document(comment.getPostID())
                .collection("Comment")
                .document(comment.getCommentID())
                .set(comment)
                .addOnSuccessListener(aVoid -> {
                    callback.onUpdateSuccess(comment);
                    Log.d(FlagsList.DEBUG_COMMENT_FLAG, "Comment successfully updated!");
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.w(FlagsList.DEBUG_COMMENT_FLAG, "Error updating document", e);
                });
    }
}
