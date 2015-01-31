package intentdataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.Type;

public class Intent {

	String action;
	String uri;
	String mimetype;
	
	Map<Object, Object> extras;
	private String calledClass;
	private Set<String> categories;
	private boolean isExplicit;
	
	public Intent() {
		this.extras=new HashMap<Object,Object>();
		this.categories=new HashSet<String>();
		this.isExplicit = false;
	}
	
	public void setExtra(String object, String object2){
		this.extras.put(object, object2);
	}

	public void setAction(String action) {
		this.action=action;		
	}
	public boolean isExplicit(){
		return isExplicit || calledClass !=null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"explicit\":").append("\"" + isExplicit() + "\"");
		if (calledClass != null)
			sb.append(",\"class\":").append(toClojure(calledClass));
		else {
			if (action != null)
				sb.append(",\"action\":").append(toClojure(action));
			if (uri != null)
				sb.append(",\"uri\":").append(toClojure(uri));
			if (mimetype != null)
				sb.append(",\"mimeType\":").append(toClojure(mimetype));
//			if (!extras.isEmpty())
//				sb.append(", :extras ").append(toClojure(extras));
			if (!categories.isEmpty())
				sb.append(",\"categories\":").append(toClojure(categories));
		}
		sb.append("}");
		return sb.toString();
	}

	public static String toClojure(Map map) {
		StringBuilder sb = new StringBuilder("{");
		for(Object key:map.keySet()){
			if(key==null)
				key="nil";
			sb.append(toClojure(key.toString())).append(",");
			Object value = map.get(key);
			if(value==null)
				value="nil";
			sb.append(toClojure(value.toString()));
			sb.append(",");
		}
		sb.append("}");
		return sb.toString();
	}

	public static String toClojure(String s) {
		if(s == null)
			s= "nil";
		return "\""+s.replaceAll("\"", "\\\\\"")+"\"";
	}
	public static String toClojure(Set<String> s) {
		StringBuilder sb = new StringBuilder("[");
		for(String string:s)
			sb.append(toClojure(string)).append(",");
		sb.append("]");
		return sb.toString();
	}

	public void addCategory(String cat) {
		this.categories.add(cat);		
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((calledClass == null) ? 0 : calledClass.hashCode());
		result = prime * result
				+ ((categories == null) ? 0 : categories.hashCode());
		result = prime * result + ((extras == null) ? 0 : extras.hashCode());
		result = prime * result + (isExplicit ? 1231 : 1237);
		result = prime * result
				+ ((mimetype == null) ? 0 : mimetype.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Intent other = (Intent) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (calledClass == null) {
			if (other.calledClass != null)
				return false;
		} else if (!calledClass.equals(other.calledClass))
			return false;
		if (categories == null) {
			if (other.categories != null)
				return false;
		} else if (!categories.equals(other.categories))
			return false;
		if (extras == null) {
			if (other.extras != null)
				return false;
		} else if (!extras.equals(other.extras))
			return false;
		if (isExplicit != other.isExplicit)
			return false;
		if (mimetype == null) {
			if (other.mimetype != null)
				return false;
		} else if (!mimetype.equals(other.mimetype))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	public void init(Constant[] args, Type[] argumentTypes,
			ConstantPool constantPool) {
		if(isString(argumentTypes[0])){
			this.action = args[0].getConstantString();
		}
		if(args.length>1)
			if("Landroid/net/Uri;".equals(argumentTypes[1].getSignature())){
			this.uri=args[1].getConstantString();
			}else if("Ljava/lang/Class;".equals(argumentTypes[1].getSignature())){
				this.calledClass = (String)((ConstantClass)args[1].getValue()).getConstantValue(constantPool);
			}		
	}

	public static boolean isString(Type argumentType) {
		return "Ljava/lang/String;".equals(argumentType.getSignature());
	}

	public void setData(String uri) {
		this.uri=uri;
	}
	public void setType(String type){
		this.mimetype=type;
	}

	/**
	 * 
	 * @param constantPool 
	 * @param constant
	 * @param constant2
	 */
	public void setClassname(Constant[] args, Type[] types, ConstantPool constantPool) {
		isExplicit = true;
		if(isString(types[1]))
			this.calledClass=args[1].getConstantString();
	}

	public void setComponent(Constant constant, Type[] argumentTypes,
			ConstantPool constantPool) {
		this.isExplicit = true;
		
	}


}
