package com.example.eduforum.activity.ui.community.viewstate;

import com.example.eduforum.activity.model.post_manage.Category;
import com.example.eduforum.activity.model.post_manage.Creator;
import com.example.eduforum.activity.model.user_manage.User;
import com.example.eduforum.activity.ui.main.fragment.CreateCommunityViewState;

import java.util.List;

public class PostViewState {
    private String postId;
    private Creator creator;
    private CreateCommunityViewState community;
    private String title;
    private String content;
    private String date;
    private List<Category> tags;
    private List<String> image;
    private List<String> taggedUsers;
    private Boolean isAnonymous;
    public PostViewState(String postId, Creator creator, CreateCommunityViewState community, String title, String content, Boolean isAnonymous, String date, List<String> image, List<String> taggedUsers,  List<Category> tags) {
        this.postId = postId;
        this.creator = creator;
        this.community = community;
        this.title = title;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.date = date;
        this.tags = tags;
        this.image = image;
        this.taggedUsers = taggedUsers;
    }
    public PostViewState(){
        this.postId = null;
        this.creator = null;
        this.community = null;
        this.title = null;
        this.content = null;
        this.date = null;
        this.tags = null;
    }
    public List<String> getImage() {
        return image;
    }
    public List<String> getTaggedUsers() {
        return taggedUsers;
    }
    public String getPostId() {
        return postId;
    }
    public Boolean getIsAnonymous() {
        return isAnonymous;
    }
    public Creator getCreator() {
        return creator;
    }
    public CreateCommunityViewState getCommunity() {
        return community;
    }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public String getDate() {
        return date;
    }
    public List<Category> getTags() {
        return tags;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }
    public void setCreator(Creator creator) {
        this.creator = creator;
    }
    public void setCommunity(CreateCommunityViewState community) {
        this.community = community;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTags(List<Category> tags) {
        this.tags = tags;
    }
    public void setAnonymous(Boolean anonymous) {
        isAnonymous = anonymous;
    }
    public void setImage(List<String> image) {
        this.image = image;
    }
    public void setTaggedUsers(List<String> taggedUsers) {
        this.taggedUsers = taggedUsers;
    }
}