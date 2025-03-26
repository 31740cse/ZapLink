import { useState } from "react";
import axios from "axios";
export default function Analytics() {
    const [shortId, setShortId] = useState("");
    const [analytics, setAnalytics] = useState(null);

    const handleGetAnalytics = async () => {
        try {
            const res = await axios.get(`${import.meta.env.VITE_BACKEND_URL}/url/analytics/${shortId}`);
            setAnalytics(res.data);
        } catch (error) {
            console.error("Error fetching analytics", error);
        }
    };

    return (
        <div className="w-full max-w-md mx-auto">
            <input
                type="text"
                placeholder="Enter Short ID"
                className="w-full p-2 border rounded"
                value={shortId}
                onChange={(e) => setShortId(e.target.value)}
            />
            <button className="mt-2 w-full bg-green-500 text-white p-2 rounded" onClick={handleGetAnalytics}>
                Get Analytics
            </button>
            {analytics && (
                <div className="p-4 bg-gray-100 rounded mt-2">
                    <p>Total Clicks: {analytics.totalClicks}</p>
                    <ul>
                        {analytics.analytics.map((visit, index) => (
                            <li key={index}>{new Date(visit.timestamp).toLocaleString()}</li>
                        ))}

                    </ul>
                </div>
            )}
        </div>
    );
}