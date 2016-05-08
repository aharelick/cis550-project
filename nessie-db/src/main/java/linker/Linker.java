package linker;

import dbWrapper.DBWrapper;
import dbWrapper.TreeNode;

import java.util.List;
import java.util.Set;

/**
 * Created by joeraso on 5/3/16.
 *
 * Linker will pull trees from dynamoDB and create linkages between them
 */
public interface Linker {

    public void createLinks(Set<TreeNode> nodes);
}
