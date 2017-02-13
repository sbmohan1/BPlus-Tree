import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Collections;

/**
 * BPlusTree Class Assumptions: 1. No duplicate keys inserted 2. Order D:
 * D<=number of keys in a node <=2*D 3. All keys are non-negative
 * TODO: Rename to BPlusTree
 * GROUP: SISHIR MOHAN, WEI WANG, WODAN ZHOU
 */
public class BPlusTree<K extends Comparable<K>, T> {

	//global variables
	public Node<K,T> root;
	public static final int D = 2;

	/**
	 * TODO Search the value for a specific key
	 * 
	 * @param key
	 * @return value
	 */
	public T search(K key) 
	{
		//instantiate new node and set to root
		Node<K,T> thisNode = root;
		ArrayList<K> keysArrayList = thisNode.keys;
		//do while node is not a leaf Node
		do
		{
			int sizeOfKey = thisNode.keys.size();
			if(key.compareTo(keysArrayList.get(sizeOfKey-1)) >= 0) //last key
			{
				thisNode = ((IndexNode<K,T>) thisNode).children.get(sizeOfKey); //cast to Index Node and get children
			}
			else
			{
				for(int i = 0; i <sizeOfKey; i++)
				{
					if(key.compareTo(keysArrayList.get(i)) < 0)
					{
						thisNode = ((IndexNode<K,T>) thisNode).children.get(sizeOfKey); //cast to index node and get children 
					}
				}
			}
			
			
		}while(thisNode.isLeafNode == false);
		
		LeafNode<K,T> leaf = ((LeafNode<K,T>) thisNode); //instantiate leaf node
		int index = Collections.binarySearch(leaf.keys, key);//use imported binary search library
		if(index >= 0) //all indices are positive
		{
			return leaf.values.get(index);
		}
		else
		{
			return null; //if index < 0 meaning it does not exist 
		}

	}

	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public void insert(K key, T value) 
	{
		Entry<K,Node<K,T>> newEntry = null;
		//Entry<K,Node<K,T>> overflowed = null;
		if(root == null)
		{
			root = new LeafNode<K,T>(key, value); //set new node to the root because root is empty
			return;
		}
		
		if(root.isLeafNode) //root is leafNode
		{
			//cast root to LeafNode<K,T>
			//call helper function to insert leaf node
			LeafNode<K,T> leafNode = (LeafNode<K,T>) root;
			newEntry = insertLeafNode(leafNode, key, value);
		}
		else //root is indexNode
		{
			//cast root to IndexNode<K,T>
			//call helper function to insert index node
			//IndexNode<K,T> indexNode = (IndexNode<K,T>) root;
			newEntry = insertIndexNode(root, key, value);
		}
		
		if (newEntry != null) { 
			root = new IndexNode<K, T>(newEntry.getKey(), root, newEntry.getValue());
		}

	}
	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * helper function for insert when it is determined that the node to be inserted is a leaf node
	 * @param leafnode
	 * @param key
	 * @param value
	 */
	public Entry<K, Node<K,T>> insertLeafNode(LeafNode<K,T> current, K key, T value) 
	{
		LeafNode<K, T> leafNode = (LeafNode<K, T>) current; //cast current into leaf node
		leafNode.insertSorted(key, value); //insert
		if(leafNode.isOverflowed())  //if overflowed, call splitLeafNode function
		{
			return splitLeafNode(leafNode);
		} else 
		{
			return null;
		}
	}
	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * helper function for insert when it is determined that the node to be inserted is an index node. This
	 * function works recursively until the key is inserted into the proper index node;it also rechecks if it has
	 * to go into a leaf node, which then the insertLeadNode helper function is called
	 * @param Node current
	 * @param key
	 * @param value
	 */
	public Entry<K, Node<K,T>> insertIndexNode(Node<K,T> current, K key, T value) 
	{
		
		int sizeOfKey = current.keys.size();
		//node is a leaf node
		Entry<K, Node<K,T>> entryOverflow = null; 
		if (current.isLeafNode) 
		{
			 //mainly used for recursion because children of the index node will become leaf nodes
			LeafNode<K,T> leafOverflow = (LeafNode<K,T>) current;
			entryOverflow = insertLeafNode(leafOverflow, key, value);
			return entryOverflow;
		}
		//node is index node
		else {
			IndexNode<K, T> indexNode = (IndexNode<K, T>) current;
			ArrayList<Node<K,T>> children = indexNode.children;
 			
			if (key.compareTo(indexNode.keys.get(sizeOfKey - 1)) > 0)
			{
				//check the last 
				entryOverflow = insertIndexNode(children.get(sizeOfKey), key, value);
			}
			else 
			{
				for (int i = 0; i < sizeOfKey; i++)
				{
					if (key.compareTo(indexNode.keys.get(i)) < 0) //if -1, update isOverflow
					{
						entryOverflow = insertIndexNode(children.get(i), key, value);
						break; //once you find isOverflow node, break
					}
				}
			}
		}
		
		if (entryOverflow != null) 
		{
			IndexNode<K, T> indexNode = (IndexNode<K, T>) current;
			//overflowed helper function to take care of overflow
			overflowed(entryOverflow, current, sizeOfKey); 
			if(indexNode.isOverflowed())
			{
				return splitIndexNode(indexNode); // return a new entry which will split the index node
			}
			else
			{
				return null;
			}

		} else //entry overflow is null, so return null
		{
			return entryOverflow;
		}
		
	}
	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * helper function called for when the entry is not null overflow
	 * @param Entry <K, Node<K,T>>
	 * @param Node
	 * @param sizeOfKey
	 */
	public void overflowed(Entry<K, Node<K,T>> overFlow, Node<K,T> thisNode, int sizeOfKey)
	{
		
		IndexNode<K,T> indexNode = (IndexNode<K,T>) thisNode; //cast node to index node
		ArrayList<K> keyArrayList = indexNode.keys; //store its keys in arraylist for later access
		if(overFlow.getKey().compareTo(keyArrayList.get(sizeOfKey-1)) > 0)
		{
			//compare last key, insert if condition met
			indexNode.insertSorted(overFlow, sizeOfKey);
		}
		else //compare rest of keys
		{
			for(int i = 0; i < sizeOfKey; i++)
			{
				if(overFlow.getKey().compareTo(keyArrayList.get(i)) < 0)
				{
					indexNode.insertSorted(overFlow, i);
					break; //break when found
				}
			}
		}
		
	}

	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param leaf, any other relevant data
	 * @return the key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitLeafNode(LeafNode<K,T> leaf) {

		//ArrayList<T> valuesArrayList = new ArrayList<T> (leaf.values.subList(0, D));
		//ArrayList<K> keysArrayList = new ArrayList<K> (leaf.keys.subList(0, D));
		ArrayList<K> newleafKeys = new ArrayList<K> (leaf.keys.subList(D, 2*D+1));
		ArrayList<T> newleafValues = new ArrayList<T>(leaf.values.subList(D, 2*D+1));
		
		
		LeafNode<K,T> newLeafNode = new LeafNode<K,T>(newleafKeys, newleafValues);
		ArrayList<K> leafKeys = new ArrayList<K>(leaf.keys.subList(0, D)); //reset the keys of leaf
		ArrayList<T> leafValues = new ArrayList<T>(leaf.values.subList(0, D)); //reset values of leaf
		leaf.keys = leafKeys; //assign
		leaf.values = leafValues; //assign
		K newLeafNodeKey = newLeafNode.keys.get(0);
		
		//reassign pointers
		newLeafNode.nextLeaf = leaf.nextLeaf;
		leaf.nextLeaf = newLeafNode;
		newLeafNode.previousLeaf = leaf;
		if(newLeafNode.nextLeaf != null)
		{
			newLeafNode.nextLeaf.previousLeaf = newLeafNode;
		}
		return new AbstractMap.SimpleEntry<K, Node<K, T>>(newLeafNodeKey, newLeafNode);
	}

	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<splitingKey, RightNode>
	 *
	 * @param index, any other relevant data
	 * @return new key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitIndexNode(IndexNode<K,T> index) {

		//create new index node
		ArrayList<K> newIndexKeys = new ArrayList<K>(index.keys.subList(D+1, 2*D+1));
		
		K keyAtD = index.keys.get(D); //save key at D for later entry
		//reassign keys and nodes of index parameter
		ArrayList<K> indexKeys = new ArrayList<K>(index.keys.subList(0,D)); 
		ArrayList<Node<K,T>> indexChildrenNodes = new ArrayList<Node<K,T>>(index.children.subList(0, D+1));
		
		//assign new IndexNode
		IndexNode<K,T> newIndexNode = new IndexNode<K,T>(newIndexKeys, index.children.subList(D + 1, 2 * D + 2));
		
		index.keys = indexKeys;
		index.children = indexChildrenNodes;
		return new AbstractMap.SimpleEntry<K, Node<K, T>>(keyAtD, newIndexNode);
		
	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 *
	 * @param key
	 */
	public void delete(K key) 
	{
		int deleteIndex = delete(root, key, null); //helper delete function that uses two other functions for leaf and index nodes
		//underflow
		if (deleteIndex >= 0) 
		{ 
			root.keys.remove(deleteIndex);
		}
		if(root.keys.isEmpty()) 
		{
			//root is leaf node, it has no children so simply set to null
			if(root.isLeafNode)
			{
				root = null;
			}
			else //set root to its child node
			{
				root = ((IndexNode<K,T>) root).children.get(0);
			}
		}
	}
	/**
	 * helper method of delete that is called if key to be deleted is in leaf node
	 * @param Node<K,T> leaf represents leaf node
	 * @param key
	 * @param int sizeOfKey
	 * @return int parentInd
	 * IndexNode<K,T> parentNode
	 */
	
	public int deleteLeafNode(Node<K,T> leaf, K key, int sizeOfKey, int parentInd, IndexNode<K,T> parentNode)
	{
		//delete leafNode
		LeafNode<K,T> leafNode = (LeafNode<K,T>) leaf;
		ArrayList<K> leafKeys = leafNode.keys; //insantiate for later use
		for(int i = 0; i < sizeOfKey; i++)
		{
			K keyToDelete = leafKeys.get(i);
			if(keyToDelete.compareTo(key) == 0)
			{
				//remove key and value in leaf node where key is the same
				leafNode.keys.remove(i);
				leafNode.values.remove(i);
				break;
			}
		}
		if(leafNode.isUnderflowed() && leafNode != root)
		{
			if(parentInd > 0)
			{
				//return int from helper function 
				return handleLeafNodeUnderflow((LeafNode<K,T>) parentNode.children.get(parentInd-1), leafNode, parentNode);
			}
			else
			{
				return handleLeafNodeUnderflow(leafNode, (LeafNode<K,T>) parentNode.children.get(parentInd+1), parentNode);
			}
		}
		else
		{
			//node does not have a parent because it is the root node
			return -1;
		}
	}
	/**
	 * helper method of delete that is called if key to be deleted is index node
	 * @param Node<K,T> index represents index node
	 * @param key
	 * @param int sizeOfKey
	 * @return int parentInd
	 * IndexNode<K,T> parentNode
	 */
	public int deleteIndexNode(Node<K,T> index, int sizeOfKey, K key, int parentInd, IndexNode<K,T> parentNode)
	{
		int indexID = -1; //first set to -1, change if node is not root
		IndexNode<K,T> indexNode = (IndexNode<K,T>) index;
		if(key.compareTo(indexNode.keys.get(0)) < 0)
		{
			indexID = delete(indexNode.children.get(0), key, indexNode);
		}
		else if(key.compareTo(indexNode.keys.get(sizeOfKey-1)) >= 0)
		{
			indexID = delete(indexNode.children.get(sizeOfKey), key, indexNode); //get index node to delete
		}
		else
		{
			for(int i = 1; i <= sizeOfKey; i++)
			{
				if(key.compareTo(indexNode.keys.get(i)) < 0)
				{
					indexID = delete(indexNode.children.get(i), key, indexNode);
					break; //break after key is found
				}
			}
		}
		
		if(indexID != -1) //has parents, not root
		{
			if(index == root)
			{
				return indexID;
			}
			indexNode.keys.remove(indexID);
			if(indexNode.isUnderflowed())
			{
				//positive meaning it is not a root, call helper function
				if(parentInd > 0)
				{
					return handleIndexNodeUnderflow((IndexNode<K, T>) parentNode.children.get(parentInd - 1), indexNode, parentNode);
				}
				else
				{
					return handleIndexNodeUnderflow(indexNode, (IndexNode<K, T>) parentNode.children.get(parentInd + 1), parentNode);
				}
			}
			else
			{
				return -1;
			}
		}
		else
		{
			return -1;
		}
		
	}
	/**
	 * helper method of delete that is called to determine what type fo deletion should occur
	 * @param Node<K,T> leaf represents leaf node
	 * @param key
	 * @param int sizeOfKey
	 * @return int parentInd
	 * IndexNode<K,T> parentNode
	 */
	public int delete(Node<K, T> current, K key, IndexNode<K, T> parentNode) {
		int parentInd;
		if (current != root)
		{
			parentInd = parentNode.children.indexOf(current);
		} else
		{
			parentInd = -1;
		}
		//leaf node
		int numKeys = current.keys.size();
		if (current.isLeafNode) 
		{
			//if node is leaf, call deleteLeafNode
			return deleteLeafNode(current, key, numKeys, parentInd,parentNode);
		}

		//index node
		else 
		{
			//call helper function 
			return deleteIndexNode(current, numKeys, key, parentInd, parentNode);
		}
		
	}
	
	/**
	 * TODO redistribution helper method to redistribute leaf node
	 *
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int redistributionLeaf(LeafNode<K,T> left, LeafNode<K,T> right, IndexNode<K,T> parent, int parentInd)
	{
		//instantiate all arraylists for later use
		ArrayList<K> leftKeys = new ArrayList<K> (left.keys);
		ArrayList<T> leftValues = new ArrayList<T> (left.values);
		ArrayList<K> rightKeys = new ArrayList<K> (right.keys);
		ArrayList<T> rightValues = new ArrayList<T> (right.values);
		
		if(left.isUnderflowed()) //left node is underflow
		{
			if((leftKeys.size() + rightKeys.size()) % 2 ==1) //uneven number of nodes
			{
				//do while nodes are odd
				do
				{
					rightKeys.remove(0);
					left.insertSorted(rightKeys.get(0), rightValues.get(0));
					
					rightValues.remove(0);
				}while(rightKeys.size() - 1 != leftKeys.size());
			}
			else 
			{
				do
				{
					rightKeys.remove(0);
					left.insertSorted(rightKeys.get(0), rightValues.get(0));
					
					rightValues.remove(0);
				}while(rightKeys.size() != leftKeys.size()); //for when nodes are even
			}
			parent.keys.remove(parentInd);
			parent.keys.add(parentInd, rightKeys.get(0));
		}
		
		else //right is underflow
		{
			
			if((leftKeys.size() + rightKeys.size()) % 2 ==1) 
			{
				do
				{
					
					leftValues.remove(leftValues.size()-1);
					leftKeys.remove(leftKeys.size()-1);
					right.insertSorted(leftKeys.get(leftKeys.size()-1), leftValues.get(leftValues.size() -1));
					
				}while(rightKeys.size() <= leftKeys.size()); //condition for odd number of nodes
			}
			else 
			{
				 do{
					right.insertSorted(left.keys.get(left.keys.size() - 1), left.values.get(left.values.size() - 1));
					left.keys.remove(left.keys.size() - 1);
					left.values.remove(left.values.size() - 1);
				}while (right.keys.size() != left.keys.size()); //condition for even number of nodes
			}
			parent.keys.remove(parentInd);
			parent.keys.add(parentInd, parent.children.get(parentInd+1).keys.get(0));
		}
		return -1;	
	}
	
	
	/**
	 * TODO Handle LeafNode Underflow (merge or redistribution)
	 *
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleLeafNodeUnderflow(LeafNode<K,T> left, LeafNode<K,T> right, IndexNode<K,T> parent) 
	{
		//instantiate for later use
		ArrayList<K> leftKeys = new ArrayList<K> (left.keys);
		ArrayList<T> leftValues = new ArrayList<T> (left.values);
		ArrayList<K> rightKeys = new ArrayList<K> (right.keys);
		ArrayList<T> rightValues = new ArrayList<T> (right.values);

		//get parent index 
		int parentInd = parent.children.indexOf(left); 

		//redistribution
		if ((left.keys.size() + right.keys.size()) >= 2 * D) 
		{ 
			//redistribute if leaf is underflow
			return redistributionLeaf(left, right, parent, parentInd);
			
		}

		else 
		{
			//merge them all
			left.values.addAll(right.values);
			left.keys.addAll(right.keys);
			
			
			//reassign
			left.nextLeaf = right.nextLeaf;
			if (left.nextLeaf != null) 
			{
				left.nextLeaf.previousLeaf = left;
			}
			parent.children.remove(parentInd + 1);
			return parentInd;
		}
	}

	/**
	 * TODO redistribution helper method for redistributing index nodes
	 *
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int redistributionIndex(IndexNode<K,T> leftIndex, IndexNode<K,T> rightIndex, IndexNode<K,T> parent, int parentInd)
	{
		//instantiate for later use
		ArrayList<K> parentKeys = new ArrayList<K>(parent.keys);
		ArrayList<K> leftKeys = new ArrayList<K> (leftIndex.keys);
		ArrayList<Node<K,T>> leftChildren = new ArrayList<Node<K,T>> (leftIndex.children);
		ArrayList<K> rightKeys = new ArrayList<K> (rightIndex.keys);
		ArrayList<Node<K,T>> rightChildren = new ArrayList<Node<K,T>> (rightIndex.children);
		if (leftIndex.isUnderflowed()) {
			
			if ((leftIndex.keys.size() + rightIndex.keys.size()) % 2 == 0) //signifies when nodes are even
			{
				do 
				{
					leftKeys.add(parentKeys.get(parentInd));
					parentKeys.add(parentInd, rightIndex.keys.get(0));
					parentKeys.remove(parentInd);
					
					rightKeys.remove(0);
					leftChildren.add(rightIndex.children.get(0));
					rightChildren.remove(0);
				}while (leftKeys.size() != rightKeys.size()); //left keySize and right keySize !=
			}
			
			else //nodes are odd
			{ 
				 do{
					 //same as above but comparing leftKeys with rightKey - 1
					 leftKeys.add(parentKeys.get(parentInd));
						parentKeys.add(parentInd, rightIndex.keys.get(0));
						parentKeys.remove(parentInd);
						
						rightKeys.remove(0);
						leftChildren.add(rightIndex.children.get(0));
						rightChildren.remove(0);
				}while (leftIndex.keys.size() != rightIndex.keys.size() - 1); //while left keySize != right keySize
			}
		}
		else //rightIndex underflow
		{
			
			if ((leftIndex.keys.size() + rightIndex.keys.size()) % 2 == 0) //even nodes
			{             
				do 
				{
					int sizeOfKey = leftKeys.size();
					
					parentKeys.add(parentInd, leftIndex.keys.get(sizeOfKey - 1));
					rightKeys.add(0, parentKeys.get(parentInd));
					parentKeys.remove(parentInd);
					
					
					rightChildren.add(0, leftIndex.children.get(sizeOfKey));
					leftKeys.remove(sizeOfKey - 1);
					leftChildren.remove(sizeOfKey);
				}while (leftIndex.keys.size() != rightIndex.keys.size()); //left keysize nd right keysize !=
			}
			
			else //odd nodes
			{ 
				do 
				{
					int sizeOfKey = leftKeys.size();
					
					parentKeys.add(parentInd, leftIndex.keys.get(sizeOfKey - 1));
					rightKeys.add(0, parentKeys.get(parentInd));
					parentKeys.remove(parentInd);
					
					
					rightChildren.add(0, leftIndex.children.get(sizeOfKey));
					leftKeys.remove(sizeOfKey - 1);
					leftChildren.remove(sizeOfKey);
				}while (leftIndex.keys.size() > rightIndex.keys.size()); //left key size is higher than right
			}
		} 				
		return -1;
	}
	/**
	 * TODO Handle IndexNode Underflow (merge or redistribution)
	 *
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleIndexNodeUnderflow(IndexNode<K,T> leftIndex, IndexNode<K,T> rightIndex, IndexNode<K,T> parent) 
	{
		//Instantiate for later use
		ArrayList<K> leftKeys = new ArrayList<K>(leftIndex.keys);
		ArrayList<K> parentKeys = new ArrayList<K>(parent.keys);
		
		int parentInd = parent.children.indexOf(leftIndex); 
		
		if ((leftIndex.keys.size() + rightIndex.keys.size()) >= 2 * D) 
		{	//redistribute index 
			
			return redistributionIndex(leftIndex, rightIndex, parent, parentInd);
		}
		
		else 
		{ 	//merge all of them
			leftIndex.keys.add(parentKeys.get(parentInd));
			leftIndex.keys.addAll(rightIndex.keys);
			
			

			parent.children.remove(parentInd+ 1);
			leftIndex.children.addAll(rightIndex.children);

			return parentInd;
		}		
	}
}
