// src/utils/mailer.js
const nodemailer = require('nodemailer');

// Configure transporter (use environment variables for credentials)
const transporter = nodemailer.createTransport({
    service: process.env.EMAIL_SERVICE || 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS,
    },
});

/**
 * Send an email
 * @param {string} to - Recipient email
 * @param {string} subject - Email subject
 * @param {string} html - HTML body
 * @returns {Promise}
 */
async function sendMail(to, subject, html) {
    const mailOptions = {
        from: process.env.EMAIL_FROM || process.env.EMAIL_USER,
        to,
        subject,
        html,
    };
    return transporter.sendMail(mailOptions);
}

module.exports = { sendMail };