import { Card, CardHeader } from "@/components/ui/card";
import { PostCard } from "@/components/dashboard/post-card";
import type { Post } from "@/lib/types";

export function SimilarPostsSection({ posts }: { posts: Post[] }) {
  return (
    <Card>
      <CardHeader title="✨ 類似の伸びている投稿" description="この投稿と傾向が近く、成果を出している投稿" />
      {posts.length === 0 ? (
        <p className="text-sm text-slate-400">類似投稿が見つかりませんでした</p>
      ) : (
        <div className="space-y-2">
          {posts.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      )}
    </Card>
  );
}
