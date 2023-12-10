import styles from './page.module.css'
import {getSession} from "@/app/utils/getSession";
import {redirect} from "next/navigation";

export default async function Home() {
  const session = await getSession()
  if(!session){
    redirect('/login')
  }

  return (
    <main className={styles.main}>
      <div> <pre> {JSON.stringify(session,null,2) } </pre></div>
    </main>
  )
}
