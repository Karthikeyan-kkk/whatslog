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
			int total=mails.size();

			boolean media= Utils.getConfiguracao(gmail.getContext()).isMedia();

			for(MailMessage mail: mails){


				String subject=assunto;
				String log="log.zip";

				if(parts || media){
					if(total>1){
						subject+=" - "+i+"/"+total;
						log=String.format("log %d/%d.zip",i,total);
						i++;
					}
					gmail.addAttachment(log, mail.getZip() );
				}else{

					Set<String> keys=mail.getAnexos().keySet();

					for(String key:keys){

						byte[] anexo=mail.getAnexos().get(key);
						if(anexo!=null){
							gmail.addAttachment(key, anexo);
						}

					}
				}

				gmail.sendMail(subject, "Check the attached file", null, destinatario);
				Thread.sleep(10000);

				gmail.clearAttachments();
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