import type { Project } from '../data/projects'
import styles from './ProjectGrid.module.css'

export default function ProjectCard({ project }: { project: Project }) {
  return (
    <article className={`${styles.card} ${styles[project.size]}`}>
      <div className={`${styles.thumb} ${styles['tone' + project.tone]}`}>
        <span className={styles.index}>{project.index}</span>
      </div>
      <div className={styles.info}>
        <h3>{project.title}</h3>
        <p>
          {project.category} <span className={styles.dot}>·</span> {project.year}
        </p>
      </div>
    </article>
  )
}
