package com.akshansh.organizer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("🔹 Enter the folder path to scan:");
        String path = sc.nextLine();

        FileScanner scanner = new FileScanner();
        scanner.scanFolder(path);

        sc.close();
    }
}