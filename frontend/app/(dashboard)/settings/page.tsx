import { Card, CardHeader } from "@/components/ui/card";
import { ProfileSettingsForm } from "@/components/dashboard/settings/profile-settings-form";
import { NotificationSettingsForm } from "@/components/dashboard/settings/notification-settings-form";
import { ApiKeySettingsPanel } from "@/components/dashboard/settings/api-key-settings";
import { TeamSettingsSection } from "@/components/dashboard/settings/team-settings-section";
import { DataExportSection } from "@/components/dashboard/settings/data-export-section";
import { DeleteAccountSection } from "@/components/dashboard/settings/delete-account-section";

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <Card>
        <CardHeader title="プロフィール" description="表示名・メールアドレスの変更" />
        <ProfileSettingsForm />
      </Card>

      <Card>
        <CardHeader title="チーム" description="チームを作成してメンバーと分析結果を共有します" />
        <TeamSettingsSection />
      </Card>

      <Card>
        <CardHeader title="APIキー" description="外部連携用のAPIキーを管理します" />
        <ApiKeySettingsPanel />
      </Card>

      <Card>
        <CardHeader title="通知設定" description="メール通知のオン・オフを切り替えます" />
        <NotificationSettingsForm />
      </Card>

      <Card>
        <CardHeader title="データのエクスポート" description="保有している個人データをダウンロードします" />
        <DataExportSection />
      </Card>

      <Card>
        <CardHeader title="アカウント削除" description="アカウントとすべての関連データを完全に削除します" />
        <DeleteAccountSection />
      </Card>
    </div>
  );
}
