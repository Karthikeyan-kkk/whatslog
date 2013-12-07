package com.whatslog;

import java.util.List;
import java.util.Set;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import android.os.AsyncTask;
import android.util.Log;

import com.gmailsender.GMailSender;

class SendEmailAsyncTask extends AsyncTask<List<MailMessage> , Void, Boolean> {
	private String assunto, destinatario;
	private GMailSender gmail;
	public SendEmailAsyncTask(GMailSender gmail, String assunto,String destinatario) {
		this.gmail=gmail;
		this.assunto=assunto;
		this.destinatario=destinatario;
	}

	protected Boolean doInBackground(List<MailMessage>... anexos) {
		try {
			List<MailMessage> mails=anexos[0];

			boolean parts=mails.size()>1;
			int i=1;
			for(MailMessage mail: mails){

				Set<String> keys=mail.getAnexos().keySet();

				for(String key:keys){

					byte[] anexo=mail.getAnexos().get(key);
					if(anexo!=null){
						gmail.addAttachment(key, anexo);
					}

				}
				String subject=assunto;
				if(parts){
					subject+=" - "+i;
					i++;
				}
				gmail.sendMail(subject, "Check the attached file", null, destinatario);
			}


			return true;
		} catch (AuthenticationFailedException e) {
			Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
			e.printStackTrace();
			return false;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}