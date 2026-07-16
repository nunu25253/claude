import styles from './Hero.module.css'

export default function Hero() {
  return (
    <section id="top" className={styles.hero}>
      <div className={`shell ${styles.grid}`}>
        <p className={`eyebrow ${styles.eyebrow}`}>Visual Designer — Tokyo / Remote</p>

        <h1 className={styles.headline}>
          レイアウトで
          <br />
          物語を伝える。
        </h1>

        <div className={styles.meta}>
          <p className={styles.metaText}>
            グリッドと余白を設計の軸に、ブランド・エディトリアル・Webの領域を横断して制作しています。
            構造から生まれる説得力のあるデザインを届けます。
          </p>
          <dl className={styles.stats}>
            <div>
              <dt>Experience</dt>
              <dd>8 Years</dd>
            </div>
            <div>
              <dt>Projects</dt>
              <dd>60+</dd>
            </div>
            <div>
              <dt>Based in</dt>
              <dd>Tokyo, JP</dd>
            </div>
          </dl>
        </div>
      </div>

      <div className={`shell ${styles.marqueeWrap}`}>
        <div className={styles.marquee}>
          {Array.from({ length: 2 }).map((_, i) => (
            <span key={i}>
              Brand Identity ・ Editorial Design ・ Web Experience ・ Art Direction ・ Typeface ・&nbsp;
            </span>
          ))}
        </div>
      </div>

      <hr className={`rule ${styles.rule}`} />
    </section>
  )
}
