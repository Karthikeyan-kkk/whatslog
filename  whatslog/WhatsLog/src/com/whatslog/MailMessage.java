package com.whatslog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class MailMessage {

	private Map<String, byte[]> anexos=new HashMap<String, byte[]>();
	private byte[] zip;
	public Map<String, byte[]> getAnexos() {
		return anexos;
	}

	public void fixAnexos(){
		Map<String, byte[]> tmp= new HashMap<String, byte[]>();

		Set<String> keys=this.anexos.keySet();

		for(String key:keys){

			byte[] anexo=this.anexos.get(key);

			if(anexo!=null)
				tmp.put(key, anexo);
		}
		anexos=tmp;
	}

	public void setAnexos(Map<String, byte[]> anexos) {
		this.anexos = anexos;
	}

	public long getTamanho(){
		long tamanho=0;

		Set<String> keys=this.anexos.keySet();

		for(String key:keys){

			byte[] anexo=this.anexos.get(key);

			tamanho+=anexo.length;
		}

		return tamanho;
	}

	public byte[] getZip(){
		if(zip==null){
			fixAnexos();
			zip= Utils.compactar(getAnexos());
		}
		return zip;
	}
}