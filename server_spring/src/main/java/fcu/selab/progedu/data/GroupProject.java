package fcu.selab.progedu.data;

import java.util.Date;

public class GroupProject {
  private int id = 0;

  private String name = "";

  private Date releaseTime = null;

  private Date createTime = null;

  private Date deadline = null;

  private String description = "";

  private ProjectTypeEnum type;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getDeadline() {
    return deadline;
  }

  public void setDeadline(Date deadline) {
    this.deadline = deadline;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ProjectTypeEnum getType() {
    return type;
  }

  public ProjectTypeEnum setType(ProjectTypeEnum type) {
    return this.type = type;
  }

  public Date getReleaseTime() {
    return releaseTime;
  }

  public void setReleaseTime(Date releaseTime) {
    this.releaseTime = releaseTime;
  }

  @Override
  public String toString() {
    return "GroupProject{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", releaseTime=" + releaseTime +
            ", createTime=" + createTime +
            ", deadline=" + deadline +
            ", description='" + description + '\'' +
            ", type=" + type +
            '}';
  }
}
