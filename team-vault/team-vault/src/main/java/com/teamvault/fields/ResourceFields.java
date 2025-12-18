package com.teamvault.fields;

public class ResourceFields {

	private ResourceFields() {}
	
	public static final String ID = "_id";
	
	public static final String USER_ID = "user._id";
	
	public static final String RESOURCE_TITLE = "resourceDetails.title";
	
	public static final String RESOURCE_DESCRIPTION = "resourceDetails.description";
	
	public static final String IS_DELETED = "isDeleted";
	
	public static final String VISIBLITY = "resourceVisiblity";
	
	public static final String GROUP_MEMBER_ID = "groupMember._id";
	
	public static final String NO_LIKES = "resourceMeta.noLikes";
	
	public static final String NO_DISLIKES = "resourceMeta.noDislikes";
	
	public static final String NO_COMMENTS = "resourceMeta.noComments";
	
	public static final String AVG_RATING = "resourceMeta.avgRating";
	
	public static final String S3_TAGS = "s3Details.tags";
	
	public static final String FILE_SIZE = "s3Details.size";
	
	public static final String CURRENT_VERIONS_ID = "s3Details.versionId";
	
	public static final String GROUP_ID = "group._id";
}