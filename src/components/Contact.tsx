import styles from './Contact.module.css'

const LINKS = [
  { label: 'Email', value: 'hello@studiogrid.jp', href: 'mailto:hello@studiogrid.jp' },
  { label: 'Instagram', value: '@studio.grid', href: '#' },
  { label: 'X (Twitter)', value: '@studiogrid', href: '#' },
  { label: 'Note', value: 'studiogrid.note.jp', href: '#' },
]

export default function Contact() {
  return (
    <section id="contact" className={styles.section}>
      <div className="shell">
        <div className={styles.top}>
          <p className="eyebrow">Let's Work Together</p>
          <h2 className={styles.headline}>
            プロジェクトのご相談は
            <br />
            お気軽にどうぞ。
          </h2>
        </div>

        <div className={styles.grid}>
          {LINKS.map((link) => (
            <a key={link.label} href={link.href} className={styles.link}>
              <span className={styles.linkLabel}>{link.label}</span>
              <span className={styles.linkValue}>{link.value}</span>
            </a>
          ))}
        </div>
      </div>
    </section>
  )
}
