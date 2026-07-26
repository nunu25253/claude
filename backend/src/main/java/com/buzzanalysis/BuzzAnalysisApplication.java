package com.buzzanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SNS AIバズ分析プラットフォーム バックエンドAPIのエントリポイント。
 */
@SpringBootApplication
public class BuzzAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuzzAnalysisApplication.class, args);
    }
}
