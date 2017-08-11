package com.superluli.infra.email;

public interface EmailSender {
    
    public void sendCouponToUser(String userEmail, String couponCode);
    
    public void sendRPWelcomeEmailToUser(String userEmail);
    
    public void sendRPExternalWelcomeEmailToUser(String userEmail);
}
