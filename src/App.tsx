import Header from './components/Header'
import Hero from './components/Hero'
import ProjectGrid from './components/ProjectGrid'
import About from './components/About'
import Contact from './components/Contact'
import Footer from './components/Footer'

export default function App() {
  return (
    <>
      <Header />
      <main>
        <Hero />
        <ProjectGrid />
        <About />
      </main>
      <Contact />
      <Footer />
    </>
  )
}
