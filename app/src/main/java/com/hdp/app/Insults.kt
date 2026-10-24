package com.hdp.app

object Insults {
    private val a = listOf("Dale, inútil", "Mirá vos, pelotudo", "Ay, qué boludo que sos", "Che, tarado", "Escuchame, mogólico",
        "Uh, pedazo de salame", "Bo, forro", "Atendeme, nabo", "Qué hacés, pajero", "Despertate, zapallo")
    private val b = listOf("¿en serio escribiste eso?", "tenés menos futuro que un helado en el horno",
        "ni tu mamá te banca", "contestá de una vez, que no tengo todo el día",
        "dejá el celu y andá a hacer algo útil", "tu cerebro está de vacaciones permanentes",
        "sos un desastre con patas", "das más lástima que fideo sin salsa",
        "tu WiFi tiene más personalidad que vos", "andá a lavarte el orto y volvé")
    private val c = listOf("Hijo de puta.", "Puto el que lee.", "Andá a cagar.", "Qué asco de persona.", "Soltame, boludo.", "Dejame de joder.")
    fun random() = "${a.random()}, ${b.random()} ${c.random()}"
}
