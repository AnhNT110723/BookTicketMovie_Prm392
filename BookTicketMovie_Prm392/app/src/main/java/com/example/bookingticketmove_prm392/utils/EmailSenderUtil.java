package com.example.bookingticketmove_prm392.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSenderUtil  extends AsyncTask<Void, Void, Boolean> {
    private final String subject;
    private final String body;
    private final String toEmail;
    private final Context context;
    private final String fromEmail = "anhnthe172115@fpt.edu.vn"; // bạn
    private final String password = "wfnk uwfc llxo yazo"; // app password, không phải pass Gmail

    public EmailSenderUtil(Context context, String toEmail, String subject, String body) {
        this.context = context;
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(fromEmail, password);
                        }
                    });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            return true;
        } catch (Exception e) {
            Log.e("MailSender", "Gửi mail lỗi", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        Toast.makeText(context, success ? "Gửi mail thành công" : "Gửi mail thất bại", Toast.LENGTH_SHORT).show();
    }
}
