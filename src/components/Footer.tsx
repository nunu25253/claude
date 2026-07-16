import styles from './Footer.module.css'

export default function Footer() {
  return (
    <div className={styles.bar}>
      <div className={`shell ${styles.inner}`}>
        <span>© {new Date().getFullYear()} Studio Grid. All rights reserved.</span>
        <a href="#top">Back to top ↑</a>
      </div>
    </div>
  )
}
