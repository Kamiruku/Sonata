import android.os.Parcel
import android.os.Parcelable

class Song {
    var iD: Long = 0
        private set
    var title: String? = null
        private set
    var artist: String? = null
        private set
    var path: String? = null
        private set
    var album: String? = null
        private set

    constructor(
        songID: Long,
        songTitle: String?,
        songArtist: String?,
        songAlbum: String?,
        songData: String?,
    ) {
        this.iD = songID
        this.title = songTitle
        this.artist = songArtist
        this.album = songAlbum
        this.path = songData
    }
}