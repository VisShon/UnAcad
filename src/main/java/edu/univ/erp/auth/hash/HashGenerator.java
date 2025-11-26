package edu.univ.erp.auth.hash;

import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        System.out.println("adminpass: " + BCrypt.hashpw("adminpass", BCrypt.gensalt(12)));
        System.out.println("instpass: " + BCrypt.hashpw("instpass", BCrypt.gensalt(12)));
        System.out.println("stupass: " + BCrypt.hashpw("stupass", BCrypt.gensalt(12)));
        System.out.println("stupass2: " + BCrypt.hashpw("stupass2", BCrypt.gensalt(12)));
    }
}