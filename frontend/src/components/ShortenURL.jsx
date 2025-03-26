import { useState } from "react";
import axios from "axios";
export default function ShortenURL() {
    const [longUrl, setLongUrl] = useState("");
    const [shortUrl, setShortUrl] = useState(null);

    const handleShorten = async () => {
        try {
            const res = await axios.post(`${import.meta.env.VITE_BACKEND_URL}/url`, { url: longUrl });
            // const res = await axios.post(`../../${process.env.backend_url}/url`, { url: longUrl });
            // console.log(res);

            setShortUrl(`${import.meta.env.VITE_BACKEND_URL}/${res.data.id}`);
        } catch (error) {
            console.error("Error shortening URL", error);
        }
    };

    return (
        <div className="w-full max-w-md mx-auto">
            <input
                type="text"
                placeholder="Enter long URL"
                className="w-full p-2 border rounded"
                value={longUrl}
                onChange={(e) => setLongUrl(e.target.value)}
            />
            <button className="mt-2 w-full bg-blue-500 text-white p-2 rounded" onClick={handleShorten}>
                Shorten URL
            </button>
            {shortUrl && <p className="mt-2">Short URL: <a href={shortUrl} className="text-blue-600" target="_blank" rel="noopener noreferrer">{shortUrl}</a></p>}
        </div>
    );
}