const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();


exports.cleanUpExpiredEvents = functions.pubsub.schedule("every 60 minutes").onRun(async (context) => {
    const now = admin.firestore.Timestamp.now();
    const eventsRef = db.collection("events");

    try {
        const snapshot = await eventsRef.where("endTime", "<", now).get();
        console.log("Current Timestamp:", now.toDate());
        console.log(snapshot.size);

        if (snapshot.empty) {
            console.log("No expired events to delete.");
            return null;
        }

        const batch = db.batch();
        snapshot.docs.forEach((doc) => {
            batch.delete(doc.ref);
        });

        await batch.commit();
        console.log("Expired events deleted successfully.");
    } catch (error) {
        console.error("Error deleting expired events:", error);
    }

    return null;
});
