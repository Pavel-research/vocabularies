package org.raml.vocabularies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.raml.vocabularies.dto.ClassTermDTO;

public class ClassTerm extends BaseTerm<ClassTermDTO> implements DataType{

	public ClassTerm(String name, Vocabulary vocabulary, ClassTermDTO dto) {
		super(name, vocabulary, dto);
	}
	
	protected LinkedHashMap<String,PropertyTerm>pmap=new LinkedHashMap<>();
	protected ArrayList<ClassTerm>superTypes=new ArrayList<>();
	protected ArrayList<ClassTerm>subTypes=new ArrayList<>();
	
	public PropertyTerm getDeclaredProperty(String name) {
		return this.pmap.get(name);
	}
	public PropertyTerm getProperty(String name) {
		return this.getKnownProperties().get(name);
	}
	public Collection<PropertyTerm>getDeclaredProperties(){
		return this.pmap.values();
	}
	public Collection<ClassTerm> getSuperTypes(){
		return this.superTypes;
	}
	public List<ClassTerm>getSubTypes(){
		return this.subTypes;
	}
	public boolean isSubTypeOf(DataType t) {		
		for (ClassTerm ta:this.superTypes) {
			if (ta==t||ta.isSubTypeOf(t)) {
				return true;
			}
		}
		return t==Builtins.OBJECT;
	}
	
	
	public Map<String,PropertyTerm> getKnownProperties() {
		LinkedHashMap<String, PropertyTerm>props=new LinkedHashMap<>();
		this.superTypes.forEach(v->{
			props.putAll(v.getKnownProperties());
		});
		props.putAll(this.pmap);
		return props;
	}
	
	@SuppressWarnings("unchecked")
	protected void resolve() {
		if (this.dto==null) {
			return;
		}
		this.dto.getProperties().forEach(p->{
			PropertyTerm resolveProperty = this.getVocabulary().resolveProperty(p);
			if (resolveProperty==null) {
				throw new IllegalStateException("Can not resolve property:"+p);
			}
			this.pmap.put(p, resolveProperty);
		});
		Object obj=this.dto.getExtends();
		if (obj!=null&&!(obj instanceof List<?>)) {
			obj=Collections.singletonList(obj);
		}
		if (obj!=null) {
			List<String>l=(List<String>) obj;
			l.forEach(c->{
				DataType resolveClass = this.getVocabulary().resolveClass(c);
				if (!(resolveClass instanceof ClassTerm)) {
					throw new IllegalStateException("Can not resolve super type:"+c);
				}
				this.superTypes.add((ClassTerm) resolveClass);
				((ClassTerm)resolveClass).subTypes.add(this);
			});
		}
	}
}
