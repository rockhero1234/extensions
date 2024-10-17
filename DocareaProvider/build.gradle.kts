version = 1


cloudstream {
    language = "em"
    // All of these properties are optional, you can safely remove them

    description = "Documentaries"
    authors = listOf("Dilip")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "Documentary"
    )

    iconUrl = "https://i.ibb.co/SwQRhsn/20241016-074942.jpg"
    
}
