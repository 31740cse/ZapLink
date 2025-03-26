const express = require('express')
const urlRoute = require('./routes/url');
const { connectToMongoDB } = require("./connect");
require('dotenv').config();
const URL = require('./models/url');
const app = express();
const cors = require('cors');
app.use(cors());

const PORT = 5000;
connectToMongoDB(process.env.MONGO_URI).then(() => {
    console.log("Mongodb connected");

})
app.use(express.json());
app.use("/url", urlRoute);
const isValidUrl = (url) => {
    const urlPattern = /^(https?:\/\/)?([\w.-]+)\.([a-z]{2,6})([/\w .-]*)*\/?$/i;
    return urlPattern.test(url);
};
app.get('/:shortid', async (req, res) => {
    const shortId = req.params.shortid;
    // console.log(shortId);

    const entry = await URL.findOneAndUpdate({
        shortId
    }, {
        $push: {
            visitHistory: {
                timestamp: Date.now(),
            },
        },
    });
    if (entry.redirectURL == null) {
        res.send(400).send("Invalid URL");
    }
    if (!isValidUrl(entry.redirectURL)) {
        res.status(400).send("Invalid URL Format");
    }

    res.redirect(entry.redirectURL);
})

app.listen(PORT, () => {
    console.log(`Server started at PORT: ${PORT}`);
})