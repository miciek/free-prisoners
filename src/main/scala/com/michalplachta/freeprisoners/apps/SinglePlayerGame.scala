package com.michalplachta.freeprisoners.apps
import com.michalplachta.freeprisoners.free.apps.{SinglePlayerApp => FreeApp}
import com.michalplachta.freeprisoners.freestyle.apps.{
  SinglePlayerApp => FreestyleApp
}

object SinglePlayerGame {
  def main(args: Array[String]): Unit = {
    FreeApp.main(args)
    FreestyleApp.main(args)
  }
}
