import React from "react";
import Header from "./components/Header";
import ShortenURL from "./components/ShortenURL";
import Analytics from "./components/Analytics";

export default function App() {
    return (
        <div className="flex flex-col items-center p-6 space-y-4">
            <Header />
            <ShortenURL />
            <Analytics />
        </div>
    );
}