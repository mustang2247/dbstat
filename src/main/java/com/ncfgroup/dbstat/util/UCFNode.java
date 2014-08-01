package com.ncfgroup.dbstat.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UCFNode
{
  private String name = null;
  private Object data = null;
  private Map<String, Object> attrtable = null;
  private List<UCFNode> children = null;
  private Map<String, List<UCFNode>> childtable = null;
  
  public UCFNode(String name,Object data){
    this(name,data,null,null);
  }

  public UCFNode(String name,Object data,List<UCFNode> clist){
    this(name,data,clist,null);
  }

  private UCFNode(String n, Object d, List<UCFNode> clist,Map<String, Object> atable)
  {
    this.name = n;
    this.data = d;

    if (clist != null && clist.size() > 0) {
      this.children = new LinkedList<UCFNode>();
      this.childtable = new HashMap<String, List<UCFNode>>(5);
      for (UCFNode node : clist) {
        UCFNode cnode = node.clone();
        List<UCFNode> ctlist = (List<UCFNode>)this.childtable.get(cnode.getName());
        if (ctlist == null) {
          ctlist = new ArrayList<UCFNode>(5);
          this.childtable.put(cnode.getName(), ctlist);
        }
        ctlist.add(cnode);
        this.children.add(cnode);
      }
    }
    
    if (atable != null && atable.size() > 0) {
      this.attrtable = new HashMap<String, Object>(atable);
    }
  }

  public UCFNode clone()
  {
    return new UCFNode(this.name, this.data, this.children, this.attrtable);
  }
  
  public String getName()
  {
    return this.name;
  }

  public Object getData()
  {
    return this.data;
  }
  
  public String getDataAsString()
  {
    return convertToString(getData());
  }
  
  private static String convertToString(Object obj)
  {
    if (obj != null) {
      return obj.toString();
    }
    return null;
  }

  public Object getData(String key)
  {
    if (key != null) {
      UCFNode node = getNode(key);
      if (node != null) {
        return node.getData();
      }
      return null;
    }
    return this.data;
  }

  public UCFNode getNode(String key)
  {
    if (this.childtable != null && !StringUtil.isNullOrBlank(key)) {
      return (UCFNode)this.childtable.get(key);
    }
    return null;
  }

  public void addNode(UCFNode child) {
    if (children == null) {
        children = new ArrayList<UCFNode>();
    }
    children.add(child);
  }

  public void addNode(int index, UCFNode child) {
    if (index < 0) {
      return;
    } else if (index == 0) {
      addNode(child);
    } else {
      children.add(index, child);
    }
  }

  public void removeNode(int index) {
    children.remove(index);
  }
  
  public void removeNode(String key){
    childtable.remove(key);
  }

  public UCFNode getAttribute(String key){
    if (this.attrtable != null && key != null) {
      return (UCFNode) this.attrtable.get(key);
    }
    return null;
  }

  public void setAttribute(String key, Object val){
    if (!StringUtil.isNullOrBlank(key)) {
      if (this.attrtable == null) {
        this.attrtable = new HashMap<String, Object>(5);
      }
      this.attrtable.put(key, val);
    } else {
      return;
//      throw new Exception("Invalid attribute key '" + key + "' for YoyoDataNode");
    }
  }
  
  public void removeAttribute(String key){
    if (this.attrtable != null && !StringUtil.isNullOrBlank(key))
      this.attrtable.remove(key);
  }
  
  
  
  
  
  

}