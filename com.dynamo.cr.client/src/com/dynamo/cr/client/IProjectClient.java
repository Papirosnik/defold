package com.dynamo.cr.client;

import java.io.InputStream;

import com.dynamo.cr.protocol.proto.Protocol.ApplicationInfo;
import com.dynamo.cr.protocol.proto.Protocol.BranchList;
import com.dynamo.cr.protocol.proto.Protocol.BranchStatus;
import com.dynamo.cr.protocol.proto.Protocol.LaunchInfo;
import com.dynamo.cr.protocol.proto.Protocol.ProjectInfo;

public interface IProjectClient extends IClient {

    public long getProjectId();

    public void deleteBranch(String branch)
            throws RepositoryException;

    public BranchList getBranchList()
            throws RepositoryException;

    public BranchStatus getBranchStatus(String branch)
            throws RepositoryException;

    public void createBranch(String branch)
            throws RepositoryException;

    public LaunchInfo getLaunchInfo() throws RepositoryException;

    public ApplicationInfo getApplicationInfo(String platform) throws RepositoryException;

    public InputStream getApplicationData(String platform) throws RepositoryException;

    public ProjectInfo getProjectInfo() throws RepositoryException;

}
