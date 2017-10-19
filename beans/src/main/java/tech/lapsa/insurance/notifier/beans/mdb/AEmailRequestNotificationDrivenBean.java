package tech.lapsa.insurance.notifier.beans.mdb;

import static tech.lapsa.insurance.notifier.beans.Constants.*;

import java.util.Locale;
import java.util.Properties;

import javax.annotation.Resource;

import com.lapsa.insurance.domain.Request;

import tech.lapsa.insurance.notifier.beans.NotificationMessages;
import tech.lapsa.insurance.notifier.beans.NotificationTemplates;
import tech.lapsa.javax.mail.MailBuilderException;
import tech.lapsa.javax.mail.MailException;
import tech.lapsa.javax.mail.MailFactory;
import tech.lapsa.javax.mail.MailMessageBuilder;
import tech.lapsa.lapsa.text.TextFactory;
import tech.lapsa.lapsa.text.TextFactory.TextModelBuilder.TextModel;

public abstract class AEmailRequestNotificationDrivenBean<T extends Request> extends ARequestNotificationDrivenBean<T> {

    AEmailRequestNotificationDrivenBean(final Class<T> cls) {
	super(cls);
    }

    protected abstract MailFactory mailFactory();

    protected abstract MailMessageBuilder recipients(MailMessageBuilder builder, Request request)
	    throws MailBuilderException;

    protected abstract NotificationMessages getSubjectTemplate();

    protected abstract NotificationTemplates getBodyTemplate();

    @Resource(lookup = JNDI_RESOURCE_CONFIGURATION)
    private Properties configurationProperties;

    @Override
    protected void sendWithModel(TextModel textModel, T request) {
	try {
	    Locale locale = locale(request);

	    MailMessageBuilder template = mailFactory()
		    .newMailBuilder();

	    String subject = TextFactory.newTextTemplateBuilder() //
		    .buildFromPattern(getSubjectTemplate().regular(locale)) //
		    .merge(textModel) //
		    .asString();
	    template.withSubject(subject);

	    String body = TextFactory.newTextTemplateBuilder() //
		    .buildFromInputStream(getBodyTemplate().getResourceAsStream(locale)) //
		    .merge(textModel) //
		    .asString();
	    template.withHtmlPart(body);

	    recipients(template, request)
		    .build()
		    .send();

	} catch (MailException e) {
	    throw new RuntimeException("Failed to create or send email", e);
	}
    }

}
