package com.jetbrains.heroku.git;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRemote;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author mh
 * @since 19.12.11
 */
public class GitRemoteHandler11 implements GitRemoteHandler{
    private final static Logger LOG = Logger.getInstance(GitRemoteHandler11.class);

    static class GitRepositoryInfo {
        private static boolean isInterface;
        private static Class repositoryClass = classFor("git4idea.repo.GitRepository");
        private static Method updateMethod;
        private static Method getRemotesMethod;
        private static Class topicClass;
        private static Object topics;

        static {
            initialize();
        }

        static void initialize() {
            isInterface = repositoryClass.isInterface();
            updateMethod = getUpdateMethod();
            getRemotesMethod = getMethod("getRemotes");
        }

        private static Method getUpdateMethod() {
            try {
                return getMethod("update");
            } catch(IllegalStateException ise) {
                topicClass = classFor("git4idea.repo.GitRepository$TrackedTopic");
                topics = Array.newInstance(topicClass,1);
                Array.set(topics,0, getTopic(topicClass, "CONFIG"));
                return getMethod("update", topics.getClass());
            }
        }

        private final Object repository;

        GitRepositoryInfo(Object repository) {
            if (!repositoryClass.isInstance(repository)) throw new IllegalStateException(repository+" is not an instance of "+repositoryClass);
            this.repository = repository;
        }

        public void update() {
            if (topics==null) {
                invoke(updateMethod);
            } else {
                invoke(updateMethod, topics);
            }
        }

        public Collection<GitRemote> getRemotes() {
            return (Collection<GitRemote>)invoke(getRemotesMethod);
        }
        
        private static Method getMethod(String name, Class... args) {
            try {
                return repositoryClass.getMethod(name,args);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Error retrieving method "+name+" from "+repositoryClass,e);
            }
        }

        private Object invoke(Method method, Object...args) {
            try {
                return method.invoke(repository,args);
            } catch (Exception e) {
                throw new RuntimeException("Error invoking method "+method.getName()+" on "+repository,e);
            }
        }
        

        private static Object getTopic(Class topicClass, String name) {
            for (Object topic : topicClass.getEnumConstants()) {
                if (((Enum)topic).name().equals(name)) {
                    return topic;
                }
            }
            return null;
        }

        private static Class classFor(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void updateRepository(Project project) {
        GitRepositoryInfo repository = getRepository(project);
        repository.update();
    }

    public GitRemoteInfo findRemote(String pattern, final Project project) {
        if (pattern == null) return null;
        final List<GitRemoteInfo> remotes = getRemotes(project);
        for (GitRemoteInfo remote : remotes) {
            if (remote.getUrl().matches(pattern) || remote.getName().matches(pattern)) return remote;
        }
        return null;
    }

    @Override
    public GitRemoteInfo findOrigin(String origin, Project project) {
        if (origin == null) return null;
        final List<GitRemoteInfo> remotes = getRemotes(project);
        for (GitRemoteInfo remote : remotes) {
            if (remote.getName().equalsIgnoreCase(origin)) return remote;
        }
        return null;
    }

    public List<GitRemoteInfo> getRemotes(final Project project) {
        final GitRepositoryInfo repo = getRepository(project);
        if (repo == null) return Collections.emptyList();
        final ArrayList<GitRemoteInfo> result = new ArrayList<GitRemoteInfo>();
        for (GitRemote remote : repo.getRemotes()) {
            result.add(new NewGitRemoteInfo(remote));
        }
        return result;
    }

    @Override
    public boolean isGitEnabled(Project project) {
        return getRepository(project)!=null;
    }

    static class RepositoryManagerReflection {
        private static final Class type = getRepositoryManager();
        private static final Method getRepositoryForRoot = getRepositoryForRootMethod(type);

        public static Object getRepositoryForRoot(Project project) {
            final Object repositoryManager = project.getComponent(type);
            if (repositoryManager==null) {
                LOG.error("Could not retrieve repository manager for project "+project.getName()+" vcsRoot "+project.getBaseDir());
            }
            final Object repository = invoke(project, repositoryManager);
            if (repository==null) {
                LOG.error("Could not retrieve repository for project "+project.getName()+" vcsRoot "+project.getBaseDir());
            }
            return repository;
        }

        private static Object invoke(Project project, Object repositoryManager) {
            try {
                return getRepositoryForRoot.invoke(repositoryManager, project.getBaseDir());
            } catch (IllegalAccessException e) {
                LOG.error("Error getting repository for project",e);
            } catch (InvocationTargetException e) {
                LOG.error("Error getting repository for project",e);
            }
            return null;
        }

        private static Method getRepositoryForRootMethod(Class type) {
            try {
                return type.getMethod("getRepositoryForRoot",VirtualFile.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        private static Class<?> getRepositoryManager() {
            try {
                return Class.forName("git4idea.repo.GitRepositoryManager");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private GitRepositoryInfo getRepository(final Project project) {
        return new GitRepositoryInfo(RepositoryManagerReflection.getRepositoryForRoot(project));
    }

    private static class NewGitRemoteInfo implements GitRemoteInfo {

        private final GitRemote remote;

        public NewGitRemoteInfo(GitRemote remote) {
            this.remote = remote;
        }

        public String getName() {
            return remote.getName();
        }

        public String getUrl() {
            return remote.getFirstUrl();
        }

        @Override
        public String toString() {
            return getName()+":"+getUrl();
        }
    }
}
