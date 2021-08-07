package com.jumbodinosaurs.webserver.post.auth;

import com.google.gson.JsonObject;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.util.objects.PostRequest;
import com.jumbodinosaurs.webserver.auth.server.AuthToken;
import com.jumbodinosaurs.webserver.auth.server.User;
import com.jumbodinosaurs.webserver.auth.server.captcha.CaptchaResponse;
import com.jumbodinosaurs.webserver.auth.util.AuthSession;
import com.jumbodinosaurs.webserver.auth.util.AuthUtil;
import com.jumbodinosaurs.webserver.netty.handler.http.util.HTTPResponse;
import com.jumbodinosaurs.webserver.netty.handler.http.util.header.HTTPHeader;
import com.jumbodinosaurs.webserver.netty.handler.http.util.header.HeaderUtil;
import com.jumbodinosaurs.webserver.post.PostCommand;
import com.jumbodinosaurs.webserver.util.PasswordStorage;

import java.io.IOException;
import java.time.LocalDateTime;

public class ReSendActivationEmail extends PostCommand
{
    @Override
    public HTTPResponse getResponse(PostRequest request, AuthSession authSession)
    {
        /*
         * Process for sending a new Activation Email
         *
         * Make sure the account is not activated
         * Check/Verify PostRequest Attributes
         * Get user From Auth Session
         * Prepare Activation Email
         * Update User with new activation code
         * Send New Activation Email
         * Send 200 Okay
         *  */
    
        HTTPResponse response = new HTTPResponse();
    
    
        // Make sure the account is not activated
        if(authSession.getUser().isActive())
        {
            HTTPHeader jsonApplicationTypeHeader = HeaderUtil.contentTypeHeader.setValue("json");
            JsonObject object = new JsonObject();
            object.addProperty("isActive", true);
            response.setMessage200();
            response.addHeader(jsonApplicationTypeHeader);
            response.setBytesOut(object.toString().getBytes());
            return response;
        }
    
        //Check/Verify PostRequest Attributes
    
        if(request.getCaptchaCode() == null)
        {
            response.setMessage400();
            return response;
        }
    
    
        //Verify Captcha code
    
        try
        {
            CaptchaResponse captchaResponse = AuthUtil.getCaptchaResponse(request.getCaptchaCode(),
                                                                          authSession.getDomain());
            double captchaScore = captchaResponse.getScore();
            boolean captchaSuccess = captchaResponse.isSuccess();
            if(!(captchaSuccess && captchaScore > .7))
            {
                response.setMessage409();
                return response;
            }
        }
        catch(IOException e)
        {
            LogManager.consoleLogger.error(e.getMessage());
            response.setMessage500();
            return response;
        }
    
    
        //Get user From Auth Session
    
        User currentUser = authSession.getUser();
    
    
    
        /* Prepare Activation Email
         *
         *
         * Form email with activation code
         *
         * Create Auth Token
         *
         *
         */
        
        //Form email with activation code
        int accountGracePeriod = 30;
        
        String emailActivationCode = AuthUtil.generateRandomString(100);
        
        
        //TODO Make this a link??
        String topic = "Account Activation";
        String message = "Here is your code to activate you account \n\n";
        message += emailActivationCode;
        message += "\n\n after ";
        message += accountGracePeriod + " days if your account is not activated it will be deleted.";
        
        
        //Create Auth Token
        LocalDateTime now = LocalDateTime.now();
        AuthToken emailToken;
        try
        {
            emailToken = new AuthToken(AuthUtil.emailActivationUseName,
                                       this.ip,
                                       emailActivationCode,
                                       now.plusDays(accountGracePeriod));
        }
        catch(PasswordStorage.CannotPerformOperationException e)
        {
            response.setMessage500();
            return response;
        }
        
        //Update User with new activation code
        currentUser.setToken(emailToken);
        
        //Update the User in the DataBase
        if(!AuthUtil.updateUser(authSession, currentUser))
        {
            response.setMessage500();
            return response;
        }
        
        //Send Activation Email
        //Note: For testing purposes to avoid spamming my own email we check to see if the server is in test mode
        if(!AuthUtil.testMode)
        {
            
            try
            {
                getServersEmail().sendEmail(authSession.getUser().getEmail(), topic, message);
            }
            catch(Exception e)
            {
                /*
                 * If we fail to send the code to the user's email then they can request it again
                 * so we will send 200 okay.
                 */
            }
        }
    
        //Send 200 okay
        HTTPHeader jsonApplicationTypeHeader = HeaderUtil.contentTypeHeader.setValue("json");
        JsonObject object = new JsonObject();
        object.addProperty("isActive", false);
        response.setMessage200();
        response.addHeader(jsonApplicationTypeHeader);
        response.setBytesOut(object.toString().getBytes());
        return response;
    }
    
    @Override
    public boolean requiresSuccessfulAuth()
    {
        //Note: That while we say this command does not require a successful auth still checks
        // for it's auth success should be done in in getResponse()
        return false;
    }
    
    @Override
    public boolean requiresPasswordAuth()
    {
        return false;
    }
    
    @Override
    public boolean requiresUser()
    {
        return true;
    }
}
