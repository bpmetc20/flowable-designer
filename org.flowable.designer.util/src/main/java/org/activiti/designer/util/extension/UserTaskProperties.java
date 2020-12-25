package org.activiti.designer.util.extension;

public class UserTaskProperties {
	private Long categoryId;
	private String categoryName;
    private int duration;
    private String formKey;
    private String formName;
    private String actorAssigneeId;
    private String actorAssigneeFirstName;
    private String actorAssigneeLastName;
    private String roleAssigneeId;
    private String roleAssigneeName;
    private boolean isRoleDefaultActive;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getActorAssigneeId() {
        return actorAssigneeId;
    }

    public void setActorAssigneeId(String actorAssigneeId) {
        this.actorAssigneeId = actorAssigneeId;
    }

    public String getActorAssigneeFirstName() {
        return actorAssigneeFirstName;
    }

    public void setActorAssigneeFirstName(String actorAssigneeFirstName) {
        this.actorAssigneeFirstName = actorAssigneeFirstName;
    }

    public String getActorAssigneeLastName() {
        return actorAssigneeLastName;
    }

    public void setActorAssigneeLastName(String actorAssigneeLastName) {
        this.actorAssigneeLastName = actorAssigneeLastName;
    }

    public String getRoleAssigneeId() {
        return roleAssigneeId;
    }

    public void setRoleAssigneeId(String roleAssigneeId) {
        this.roleAssigneeId = roleAssigneeId;
    }

    public String getRoleAssigneeName() {
        return roleAssigneeName;
    }

    public void setRoleAssigneeName(String roleAssigneeName) {
        this.roleAssigneeName = roleAssigneeName;
    }

    public boolean isRoleDefaultActive() {
        return isRoleDefaultActive;
    }

    public void setRoleDefaultActive(boolean roleDefaultActive) {
        isRoleDefaultActive = roleDefaultActive;
    }
    
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
