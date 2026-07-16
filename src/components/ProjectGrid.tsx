import { projects } from '../data/projects'
import ProjectCard from './ProjectCard'
import styles from './ProjectGrid.module.css'

export default function ProjectGrid() {
  return (
    <section id="work" className="shell">
      <div className="section-heading">
        <h2>Selected Work</h2>
        <span className="section-index">08 Projects — 2023–2025</span>
      </div>

      <div className={styles.grid}>
        {projects.map((project) => (
          <ProjectCard key={project.id} project={project} />
        ))}
      </div>
    </section>
  )
}
