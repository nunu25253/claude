import styles from './About.module.css'

const SKILLS = [
  ['Art Direction', 'Brand Identity', 'Editorial Layout'],
  ['Web Design', 'Design Systems', 'Typography'],
  ['Figma', 'React', 'Motion Design'],
]

export default function About() {
  return (
    <section id="about" className="shell">
      <div className="section-heading">
        <h2>About</h2>
        <span className="section-index">02 / Profile</span>
      </div>

      <div className={styles.grid}>
        <blockquote className={styles.quote}>
          「余白は、何もない場所ではなく
          <br />
          意味を運ぶための場所である。」
        </blockquote>

        <div className={styles.body}>
          <p>
            グラフィックとWebを横断しながら、10年近くレイアウト設計に向き合ってきました。
            グリッド・タイポグラフィ・余白のバランスを丁寧に組み立てることで、
            情報が自然と読み手に伝わる構造をつくることを大切にしています。
          </p>
          <p>
            国内外のブランド、出版、スタートアップと協働し、
            紙とスクリーンの両方でエディトリアルな視点を活かしたデザインを提供しています。
          </p>

          <div className={styles.skills}>
            {SKILLS.map((group, i) => (
              <ul key={i}>
                {group.map((skill) => (
                  <li key={skill}>{skill}</li>
                ))}
              </ul>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}
