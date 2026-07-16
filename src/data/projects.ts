export interface Project {
  id: string
  index: string
  title: string
  category: string
  year: string
  size: 'lg' | 'md' | 'sm' | 'wide'
  tone: 1 | 2 | 3 | 4 | 5 | 6
}

export const projects: Project[] = [
  { id: 'p1', index: '01', title: 'Kioku', category: 'Brand Identity', year: '2025', size: 'lg', tone: 1 },
  { id: 'p2', index: '02', title: 'Nagi Journal', category: 'Editorial Design', year: '2025', size: 'sm', tone: 2 },
  { id: 'p3', index: '03', title: 'Tessellate', category: 'Web Experience', year: '2024', size: 'sm', tone: 3 },
  { id: 'p4', index: '04', title: 'Sora Type', category: 'Typeface', year: '2024', size: 'wide', tone: 4 },
  { id: 'p5', index: '05', title: 'Rin Studio', category: 'Motion / Reel', year: '2024', size: 'md', tone: 5 },
  { id: 'p6', index: '06', title: 'Hako', category: 'Packaging', year: '2023', size: 'md', tone: 6 },
  { id: 'p7', index: '07', title: 'Midori Market', category: 'Web Experience', year: '2023', size: 'sm', tone: 2 },
  { id: 'p8', index: '08', title: 'Yohaku', category: 'Print / Art Direction', year: '2023', size: 'sm', tone: 3 },
]
