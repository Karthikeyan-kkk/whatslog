package com.whatslog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class MailMessage {

	private Map<String, byte[]> anexos=new HashMap<String, byte[]>();

	public Map<String, byte[]> getAnexos() {
		return anexos;
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
}