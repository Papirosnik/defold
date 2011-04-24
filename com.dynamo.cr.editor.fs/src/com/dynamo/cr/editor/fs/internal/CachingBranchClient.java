package com.dynamo.cr.editor.fs.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.dynamo.cr.client.IBranchClient;
import com.dynamo.cr.client.IClientFactory;
import com.dynamo.cr.client.RepositoryException;
import com.dynamo.cr.protocol.proto.Protocol.BranchStatus;
import com.dynamo.cr.protocol.proto.Protocol.BuildDesc;
import com.dynamo.cr.protocol.proto.Protocol.BuildLog;
import com.dynamo.cr.protocol.proto.Protocol.ResourceInfo;
import com.dynamo.cr.protocol.proto.Protocol.ResourceType;

public class CachingBranchClient implements IBranchClient {

    static class Node
    {
        /// Flags set to 1 if the meta-data for this node is known
        final static int STATE_METADATA_KNOWN = (1 << 0);
        /// Flags set to 1 if children for this node is known
        final static int STATE_METADATA_CHILDREN_KNOWN = (1 << 1);

        String path;
        String name;
        ResourceType type;
        long lastModified;
        int size;

        int state = 0;

        List<ResourceNode> children = new ArrayList<ResourceNode>();
    }

    private IBranchClient client;
    // NOTE: We use Path instead of string for automatic path normalization. ( // -> / within path, double leading slashes are preserved)
    private Map<Path, ResourceInfo> cache = new HashMap<Path, ResourceInfo>();

    public CachingBranchClient(IBranchClient client) {
        this.client = client;
    }

    private void flushPathAndChildren(IPath path) {
        Set<Path> toRemove = new HashSet<Path>();
        for (Path key : cache.keySet()) {
            if (path.isPrefixOf(key)) {
                toRemove.add(key);
            }
        }

        for (Path key : toRemove) {
            cache.remove(key);
        }
    }

    void flushAll() {
        cache.clear();
    }

    @Override
    public byte[] getResourceData(String path, String revision)
            throws RepositoryException {
        return client.getResourceData(path, revision);
    }

    @Override
    public ResourceInfo getResourceInfo(String path) throws RepositoryException {
        Path p = new Path(path);
        if (cache.containsKey(p)) {
            return cache.get(p);
        }
        else {
            ResourceInfo info = client.getResourceInfo(path);
            cache.put(p, info);
            return info;
        }
    }

    @Override
    public BranchStatus getBranchStatus() throws RepositoryException {
        return client.getBranchStatus();
    }

    @Override
    public void putResourceData(String path, byte[] bytes)
            throws RepositoryException {

        Path p = new Path(path);
        flushPathAndChildren(p);
        client.putResourceData(path, bytes);
    }

    @Override
    public void mkdir(String path) throws RepositoryException {
        client.mkdir(path);
    }

    @Override
    public void deleteResource(String path) throws RepositoryException {
        Path p = new Path(path);
        flushPathAndChildren(p);
        client.deleteResource(path);
    }

    @Override
    public void renameResource(String source, String destination)
            throws RepositoryException {
        Path sp = new Path(source);
        Path dp = new Path(destination);
        flushPathAndChildren(sp);
        flushPathAndChildren(dp);

        client.renameResource(source, destination);
    }

    @Override
    public void revertResource(String path) throws RepositoryException {
        // TODO: Currently flush all. Somewhat difficult problem...
        flushAll();
        client.revertResource(path);
    }

    @Override
    public BranchStatus update() throws RepositoryException {
        flushAll();
        return client.update();
    }

    @Override
    public void commit(String message) throws RepositoryException {
        client.commit(message);
    }

    @Override
    public void commitMerge(String message) throws RepositoryException {
        client.commitMerge(message);
    }

    @Override
    public void resolve(String path, String stage) throws RepositoryException {
        Path p = new Path(path);
        flushPathAndChildren(p);
        client.resolve(path, stage);
    }

    @Override
    public void publish() throws RepositoryException {
        client.publish();
    }

    @Override
    public BuildDesc build(boolean rebuild) throws RepositoryException {
        return client.build(rebuild);
    }

    @Override
    public BuildDesc getBuildStatus(int id) throws RepositoryException {
        return client.getBuildStatus(id);
    }

    @Override
    public BuildLog getBuildLogs(int id) throws RepositoryException {
        return client.getBuildLogs(id);
    }

    @Override
    public IClientFactory getClientFactory() {
        return client.getClientFactory();
    }

    @Override
    public URI getURI() {
        return client.getURI();
    }

}
