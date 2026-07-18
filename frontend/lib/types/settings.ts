export interface ProfileSettings {
  displayName: string;
  email: string;
}

export interface NotificationSettings {
  emailOnAnalysisComplete: boolean;
  emailWeeklyDigest: boolean;
  emailTrendingAlert: boolean;
}

export interface ApiKeySettings {
  apiKey: string | null;
  createdAt?: string;
}
