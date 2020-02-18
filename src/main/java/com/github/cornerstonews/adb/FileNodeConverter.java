package com.github.cornerstonews.adb;

import com.android.ddmlib.FileListingService.FileEntry;

class FileNodeConverter {

    static FileNode convert(FileEntry path) {
        FileNode node = new FileNode();
        node.setName(path.getName());
        node.setFullPath(path.getFullPath());
        node.setDirectory(path.isDirectory());
        node.setInfo(path.getInfo());
        node.setPermissions(path.getPermissions());
        node.setSize(path.getSize());
        node.setDate(path.getDate());
        node.setTime(path.getTime());
        node.setOwner(path.getOwner());
        node.setGroup(path.getGroup());
        node.setAppPackage(path.isApplicationPackage());
        node.setRoot(path.isRoot());
        
        if (path.isDirectory()) {
            FileEntry[] children = path.getCachedChildren();
            for (FileEntry entry : children) {
                node.addChild(convert(entry));
            }
        }
        
        return node;
    }

}
