package com.github.cornerstonews.adb;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "files")
public class FileNode {

    @XmlElement
    private String name;

    @XmlElement
    private String fullPath;

    @XmlElement
    private boolean directory;

    @XmlElement
    private String info;

    @XmlElement
    private String permissions;

    @XmlElement
    private String size;

    @XmlElement
    private String date;

    @XmlElement
    private String time;

    @XmlElement
    private String owner;

    @XmlElement
    private String group;

    @XmlElement
    private boolean appPackage;

    @XmlElement
    private boolean root;

    @XmlElement
    private List<FileNode> children;

    public FileNode() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isAppPackage() {
        return appPackage;
    }

    public void setAppPackage(boolean appPackage) {
        this.appPackage = appPackage;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public List<FileNode> getChildren() {
        return children;
    }

    public void setChildren(List<FileNode> children) {
        this.children = children;
    }

    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

    public void addChild(FileNode node) {
        if (this.children == null) {
            children = new ArrayList<FileNode>();
        }
        this.children.add(node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appPackage, date, directory, fullPath, group, info, name, owner, permissions, root, size, time);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FileNode other = (FileNode) obj;
        return appPackage == other.appPackage &&
                Objects.equals(date, other.date) &&
                directory == other.directory &&
                Objects.equals(fullPath, other.fullPath)&&
                Objects.equals(group, other.group) &&
                Objects.equals(info, other.info) &&
                Objects.equals(name, other.name) &&
                Objects.equals(owner, other.owner) &&
                Objects.equals(permissions, other.permissions) &&
                root == other.root &&
                Objects.equals(size, other.size) &&
                Objects.equals(time, other.time);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"name\": \"").append(name)
                .append("\", fullPath\": \"").append(fullPath)
                .append("\", directory\": \"").append(directory)
                .append("\", info\": \"").append(info)
                .append("\", permissions\": \"").append(permissions)
                .append("\", size\": \"").append(size)
                .append("\", date\": \"").append(date)
                .append("\", time\": \"").append(time)
                .append("\", owner\": \"").append(owner)
                .append("\", group\": \"").append(group)
                .append("\", appPackage\": \"").append(appPackage)
                .append("\", root\": \"").append(root)
                .append("\", children\": \"").append(children)
                .append("}");
        return builder.toString();
    }
    
}
