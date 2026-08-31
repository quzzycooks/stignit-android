package com.stignit.app.ui.safety

/**
 * Static first-aid reference content for the Safety Knowledge Library.
 *
 * DRAFT — sourced from public guidelines (2025 AHA CPR/ECC, American Red Cross,
 * Stop the Bleed / American College of Surgeons) as a starting point. This has
 * NOT been through clinical review by a licensed medical professional and must
 * not be treated as validated medical guidance. Do not remove this notice or
 * present this content as reviewed until that sign-off has actually happened.
 */

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

data class DrillGuide(
    val id: String,
    val title: String,
    val category: String,
    val urgency: String,
    val steps: List<String>,
    val quiz: List<QuizQuestion>,
    val note: String? = null,
)

val DRILL_GUIDES = listOf(
    DrillGuide(
        id = "cpr-adult",
        title = "CPR — Adult (Cardiac Arrest)",
        category = "Cardiac Emergency",
        urgency = "Immediate — start within seconds of confirming no breathing/response",
        steps = listOf(
            "Check the scene is safe, then check the person's responsiveness — tap firmly and shout.",
            "Check breathing. Look for normal chest rise and fall for no more than 10 seconds.",
            "If unresponsive and not breathing normally, call emergency services immediately (or send someone else to call) and get an AED if one is available nearby.",
            "Begin chest compressions immediately: push hard and fast in the center of the chest, at a rate of 100–120 compressions per minute, at least 2 inches (5 cm) deep but not more than 2.4 inches (6 cm), allowing full chest recoil between compressions.",
            "If trained, give 2 rescue breaths after every 30 compressions. If untrained or uncomfortable giving breaths, continuous hands-only compressions are still effective — keep going.",
            "If an AED arrives, turn it on and follow its voice prompts. Place pads on bare, dry skin. Stand clear during analysis and shock delivery, then resume compressions immediately afterward.",
            "Continue until emergency responders arrive and take over, or the person shows signs of life.",
        ),
        note = "These reflect the 2025 AHA Basic Life Support update — compression-first approach, minimal delay before starting compressions.",
        quiz = listOf(
            QuizQuestion(
                "What should you do first after confirming someone is unresponsive and not breathing normally?",
                listOf("Give 2 rescue breaths", "Call emergency services / get someone to call", "Check their pulse for a full minute", "Wait 5 minutes to see if they recover"),
                1, "Activating emergency response comes before starting compressions.",
            ),
            QuizQuestion(
                "What is the correct chest compression rate for adult CPR?",
                listOf("60–80 per minute", "100–120 per minute", "140–160 per minute", "As slow as possible to avoid injury"),
                1, "100–120 compressions per minute, per 2025 AHA guidelines.",
            ),
            QuizQuestion(
                "If you're untrained in rescue breaths, what should you do?",
                listOf("Stop and wait for trained help", "Continue hands-only compressions", "Attempt breaths anyway", "Only do compressions for 1 minute then stop"),
                1, "Hands-only (compression-only) CPR is still effective and recommended for untrained bystanders.",
            ),
            QuizQuestion(
                "When should you stop CPR?",
                listOf("After exactly 10 minutes", "When your arms get tired", "When emergency responders take over or the person shows signs of life", "After 30 compressions"),
                2, "",
            ),
        ),
    ),
    DrillGuide(
        id = "severe-bleeding",
        title = "Severe (Life-Threatening) Bleeding Control",
        category = "Trauma / Road Accident",
        urgency = "Immediate — within the first 1–2 minutes",
        steps = listOf(
            "Ensure the scene is safe before approaching. Call emergency services or have someone else call.",
            "Expose the wound — remove or cut away clothing covering it. Do not remove large embedded objects.",
            "Apply firm, direct pressure to the wound using clean gauze, cloth, or your hand. Press hard and hold — don't check too often.",
            "If blood soaks through, add more gauze/cloth on top without removing the original layer, and keep pressing.",
            "If bleeding is on an arm or leg and doesn't stop with direct pressure, apply a tourniquet 2–3 inches above the wound (never directly on the wound or on a joint). Tighten until bleeding stops — this will be painful for the person, which means it's working.",
            "Note and communicate the exact time the tourniquet was applied (write it on the person, tourniquet, or tell responders directly) — do not loosen or remove it once applied.",
            "If bleeding is on the head, neck, or torso, do not use a tourniquet — continue firm direct pressure and prioritize getting emergency help.",
            "Keep the person as calm and warm as possible while waiting for help, and monitor for signs of shock.",
        ),
        quiz = listOf(
            QuizQuestion(
                "Where should a tourniquet be placed relative to the wound?",
                listOf("Directly on the wound", "2–3 inches above the wound, never on a joint", "As close to the torso as possible regardless of the wound", "Below the wound, closer to the extremity's end"),
                1, "",
            ),
            QuizQuestion(
                "What should you do if blood soaks through your first layer of gauze?",
                listOf("Remove it and apply a fresh layer", "Add more gauze on top without removing the first layer", "Stop pressure and elevate only", "Apply a tourniquet immediately regardless of location"),
                1, "",
            ),
            QuizQuestion(
                "Should you use a tourniquet for a bleeding wound on the neck or torso?",
                listOf("Yes, always for severe bleeding", "No — use direct pressure only and call for help", "Only if you have training", "Only if the person requests it"),
                1, "Tourniquets are for limb bleeding only.",
            ),
            QuizQuestion(
                "Why is it important to note the time a tourniquet was applied?",
                listOf("It's not important", "So responders know how long it's been on, since prolonged use can cause tissue damage", "To know when to remove it yourself", "For insurance purposes only"),
                1, "",
            ),
        ),
    ),
    DrillGuide(
        id = "choking-adult",
        title = "Choking (Conscious Adult)",
        category = "Airway Emergency",
        urgency = "Immediate",
        steps = listOf(
            "Ask the person \"Are you choking?\" — if they can't speak, cough forcefully, or breathe, act immediately.",
            "Call for emergency help or have someone nearby call.",
            "Stand behind the person and alternate between 5 firm back blows (with the heel of your hand, between the shoulder blades) and 5 abdominal thrusts (fist above the navel, quick inward-and-upward pulls).",
            "Repeat cycles of back blows and abdominal thrusts until the object is expelled or the person can breathe/cough on their own.",
            "If the person becomes unresponsive, lower them carefully to the ground and begin CPR — check the mouth for a visible object before rescue breaths, but don't do a blind finger sweep.",
        ),
        quiz = listOf(
            QuizQuestion(
                "What's the correct response sequence for a conscious choking adult?",
                listOf("Only abdominal thrusts, no back blows", "Alternate 5 back blows and 5 abdominal thrusts", "Only back blows", "Wait for the person to cough it out on their own"),
                1, "",
            ),
            QuizQuestion(
                "What should you do if the choking person becomes unresponsive?",
                listOf("Keep doing back blows only", "Lower them to the ground and begin CPR", "Perform a blind finger sweep immediately", "Wait for emergency services without acting"),
                1, "",
            ),
            QuizQuestion(
                "Should you do a blind finger sweep to find the object?",
                listOf("Yes, always", "No — only remove an object if you can actually see it", "Only in children", "Only if trained in advanced airway management"),
                1, "A blind sweep can push the object deeper.",
            ),
        ),
    ),
    DrillGuide(
        id = "burns",
        title = "Burns",
        category = "Trauma",
        urgency = "Immediate for cooling; escalate if severe",
        steps = listOf(
            "Move the person away from the source of the burn (heat, chemical, electrical) — ensure your own safety first, especially with electrical or chemical burns.",
            "Cool the burn under cool (not ice-cold) running water for 10–20 minutes. Do not use ice, butter, or ointments.",
            "Remove tight clothing or jewelry near the burn area before swelling starts, unless it's stuck to the skin — don't force it.",
            "Cover loosely with a clean, non-stick dressing or clean cloth. Do not pop any blisters.",
            "Call emergency services immediately if: the burn is larger than the person's palm, on the face/hands/feet/groin/major joints, looks white/charred/leathery (full thickness), or was caused by chemicals/electricity.",
            "Watch for signs of shock and keep the person warm (without covering the burn tightly).",
        ),
        quiz = listOf(
            QuizQuestion(
                "What should you cool a burn with?",
                listOf("Ice cubes directly on the skin", "Cool (not ice-cold) running water for 10–20 minutes", "Butter or ointment", "Nothing — leave it dry"),
                1, "",
            ),
            QuizQuestion(
                "Should you pop blisters caused by a burn?",
                listOf("Yes, to relieve pressure", "No, never", "Only if they're large", "Only with sterile equipment"),
                1, "",
            ),
            QuizQuestion(
                "Which of these burns needs emergency medical attention?",
                listOf("A burn smaller than a coin on the fingertip", "A burn on the face larger than the person's palm", "Mild sunburn", "A burn that stopped hurting immediately"),
                1, "",
            ),
        ),
    ),
    DrillGuide(
        id = "shock",
        title = "Shock (Recognizing and Responding)",
        category = "General Emergency Response",
        urgency = "Immediate — monitor continuously",
        steps = listOf(
            "Recognize the signs: pale/cool/clammy skin, rapid weak pulse, rapid shallow breathing, confusion or restlessness, weakness, nausea.",
            "Call emergency services immediately if not already done.",
            "Lay the person down if possible. If there's no suspected head, neck, spinal, or leg injury, raise their legs slightly (about 12 inches) to help blood flow.",
            "Keep the person warm using a blanket or clothing, but avoid overheating.",
            "Do not give the person food or water, even if they ask for it.",
            "Continue to monitor breathing and responsiveness until help arrives. Begin CPR immediately if they stop breathing or become unresponsive.",
        ),
        quiz = listOf(
            QuizQuestion(
                "What are common signs of shock?",
                listOf("Warm, flushed, dry skin", "Pale, cool, clammy skin with a rapid weak pulse", "Slow deep breathing and drowsiness only", "High energy and alertness"),
                1, "",
            ),
            QuizQuestion(
                "Should you give a person in shock food or water?",
                listOf("Yes, water only", "Yes, if they ask for it", "No, never", "Only small sips of water"),
                2, "",
            ),
            QuizQuestion(
                "If there's no suspected spinal or leg injury, what can help someone in shock?",
                listOf("Sitting them fully upright", "Raising their legs about 12 inches", "Having them stand and walk around", "Wrapping them tightly to restrict movement"),
                1, "",
            ),
        ),
    ),
    DrillGuide(
        id = "stroke",
        title = "Stroke Recognition (BE-FAST)",
        category = "Medical Emergency",
        urgency = "Immediate — treatment is highly time-sensitive",
        steps = listOf(
            "Balance — sudden loss of balance or coordination.",
            "Eyes — sudden vision loss or trouble seeing in one or both eyes.",
            "Face — ask the person to smile; does one side droop?",
            "Arms — ask them to raise both arms; does one drift downward?",
            "Speech — ask them to repeat a simple sentence; is it slurred or wrong?",
            "Time — if any of these are present, note the time symptoms started and call emergency services immediately.",
            "Do not give the person food, water, or medication (including aspirin) — this can worsen certain types of stroke.",
            "Keep the person calm, seated or lying down with head slightly raised, and monitor breathing and responsiveness.",
            "If they become unresponsive and stop breathing normally, begin CPR.",
            "Tell responders exactly when symptoms started — this single detail affects what treatment is possible.",
        ),
        quiz = listOf(
            QuizQuestion(
                "What does the \"F\" in BE-FAST stand for?",
                listOf("Fever", "Face drooping", "Fainting", "Fatigue"),
                1, "",
            ),
            QuizQuestion(
                "Why shouldn't you give a possible stroke patient aspirin or food?",
                listOf("It's fine to give both", "Aspirin can worsen bleeding if it's a hemorrhagic stroke, and choking risk exists with food", "There's no reason, it's just tradition", "Only water should be avoided"),
                1, "",
            ),
            QuizQuestion(
                "Why is noting the exact time symptoms started so important?",
                listOf("It's not important", "Certain stroke treatments are only effective within specific time windows", "Only for paperwork", "To determine if it's a stroke at all"),
                1, "",
            ),
        ),
    ),
    DrillGuide(
        id = "heart-attack",
        title = "Heart Attack Warning Signs",
        category = "Cardiac Emergency",
        urgency = "Immediate",
        steps = listOf(
            "Watch for: chest pain/pressure/squeezing (may radiate to arm, jaw, back, or neck), shortness of breath, cold sweat, nausea, or lightheadedness. Note that symptoms can be less obvious in women (fatigue, nausea, back/jaw pain without classic chest pain).",
            "Call emergency services immediately — do not wait to see if symptoms pass.",
            "Have the person sit down, rest, and stay calm. Loosen tight clothing.",
            "If the person is not allergic and it's readily available, chewing one adult aspirin tablet can help — but only if you're confident it's a heart attack and not a stroke, since aspirin can worsen a bleeding stroke. When in doubt, skip it and focus on getting emergency help fast.",
            "If the person becomes unresponsive and stops breathing normally, begin CPR immediately.",
        ),
        quiz = listOf(
            QuizQuestion(
                "Which symptom pattern is more common in women having a heart attack?",
                listOf("Always classic crushing chest pain only", "Fatigue, nausea, or back/jaw pain without classic chest pain", "No symptoms at all", "Only leg pain"),
                1, "",
            ),
            QuizQuestion(
                "When is giving aspirin during a suspected heart attack risky?",
                listOf("It's never risky", "If it might actually be a stroke, since aspirin can worsen bleeding", "Only if the person is under 40", "Aspirin should never be given under any circumstance"),
                1, "",
            ),
            QuizQuestion(
                "What should you do while waiting for emergency services?",
                listOf("Have the person walk around to stay alert", "Have them sit down, rest, and loosen tight clothing", "Give them a large meal", "Leave them alone to rest privately"),
                1, "",
            ),
        ),
    ),
    DrillGuide(
        id = "recovery-position",
        title = "Unconscious but Breathing (Recovery Position)",
        category = "General Emergency Response",
        urgency = "Immediate",
        steps = listOf(
            "Confirm the person is breathing normally but unresponsive to voice or touch.",
            "Call emergency services.",
            "Kneel beside them, straighten the leg nearest you, and place their far arm across their chest.",
            "Bend their far knee up, then gently roll them toward you onto their side using the bent knee as leverage.",
            "Tilt their head back slightly to keep the airway open, and position their top hand under their cheek for support.",
            "Stay with them, monitor breathing continuously, and be ready to start CPR if breathing stops or becomes abnormal.",
            "If a road-traffic accident is involved and spinal injury is possible, avoid moving the person unless absolutely necessary (e.g. immediate danger like fire) — prioritize protecting the head/neck and wait for trained responders if breathing is currently normal.",
        ),
        quiz = listOf(
            QuizQuestion(
                "When should you place someone in the recovery position?",
                listOf("When they're unresponsive but breathing normally", "When they're fully conscious", "When you suspect a spinal injury and it's otherwise safe", "When they're actively having a seizure"),
                0, "",
            ),
            QuizQuestion(
                "What's the purpose of the recovery position?",
                listOf("To make the person more comfortable only", "To keep the airway open and let fluids drain rather than being inhaled", "To warm the person up", "To restrain their movement"),
                1, "",
            ),
            QuizQuestion(
                "If a road accident victim is breathing normally but you suspect spinal injury, what should you generally do?",
                listOf("Immediately roll them into recovery position", "Avoid moving them unless there's immediate danger (fire, etc.)", "Sit them upright right away", "Move them to a more comfortable spot"),
                1, "",
            ),
        ),
    ),
    DrillGuide(
        id = "seizures",
        title = "Seizures",
        category = "Medical Emergency",
        urgency = "Immediate during the seizure",
        steps = listOf(
            "Stay calm and time the seizure from when it starts.",
            "Clear the area of anything hard or sharp the person could hit. Do not hold them down or try to stop their movements.",
            "Do not put anything in their mouth — this is a myth and can cause injury.",
            "Cushion their head with something soft if possible.",
            "Once convulsions stop, roll them into the recovery position and check breathing.",
            "Call emergency services if: the seizure lasts longer than 5 minutes, a second seizure starts right after, the person doesn't regain consciousness afterward, it happens in water, or the person is injured, pregnant, or has no known history of seizures.",
            "Stay with them until they're fully alert or help arrives — they may be confused or embarrassed afterward.",
        ),
        quiz = listOf(
            QuizQuestion(
                "Should you hold down someone having a seizure to stop their movements?",
                listOf("Yes, to prevent injury", "No — clear the area around them instead", "Only their arms", "Only if they're a child"),
                1, "",
            ),
            QuizQuestion(
                "Should you put something in the person's mouth during a seizure?",
                listOf("Yes, to prevent tongue-biting", "No — this is a myth and can cause injury", "Only a soft object", "Only if trained"),
                1, "",
            ),
            QuizQuestion(
                "When should you call emergency services for a seizure?",
                listOf("Only if it happens more than 3 times in a year", "If it lasts longer than 5 minutes, repeats immediately, or the person doesn't regain consciousness", "Every single time, no exceptions needed for context", "Never — seizures always resolve on their own"),
                1, "",
            ),
        ),
    ),
    DrillGuide(
        id = "rta-scene-safety",
        title = "Road Traffic Accident — Scene Safety Before First Aid",
        category = "Road Accident",
        urgency = "Immediate — before any hands-on first aid",
        steps = listOf(
            "Before approaching, check the scene for ongoing danger: oncoming traffic, fire, leaking fuel, unstable vehicle position, downed power lines.",
            "If safe, turn on hazard lights, and use a warning triangle or other visible marker if available to alert oncoming traffic.",
            "Call emergency services and give the exact location before doing anything else, since responders need this immediately even if you're still assessing the scene.",
            "Do not move injured people unless there is an immediate life threat (fire, danger of further collision, sinking vehicle) — moving someone with a possible spinal injury can cause permanent harm.",
            "If someone must be moved due to immediate danger, try to keep their head, neck, and spine aligned as much as possible while moving them as a unit.",
            "Check each person for responsiveness and breathing. Prioritize anyone not breathing (start CPR) or with severe bleeding (apply pressure/tourniquet) over less urgent injuries.",
            "Keep bystanders back to avoid further injury and to keep the area clear for arriving responders.",
        ),
        quiz = listOf(
            QuizQuestion(
                "What should you check for before approaching an accident scene?",
                listOf("Nothing, just approach immediately", "Ongoing danger like traffic, fire, fuel leaks, or downed power lines", "Only whether the victim is conscious", "Whether bystanders are filming"),
                1, "",
            ),
            QuizQuestion(
                "When should you give your location to emergency services?",
                listOf("Only after fully assessing all injuries", "Immediately, before completing your assessment", "It's not necessary if you describe the accident well", "Only if asked directly"),
                1, "",
            ),
            QuizQuestion(
                "Should you move an injured person with a possible spinal injury?",
                listOf("Always, to a more comfortable position", "Only if there's immediate danger like fire, and carefully as a unit", "Never, under any circumstances", "Only if they ask to be moved"),
                1, "",
            ),
        ),
    ),
    DrillGuide(
        id = "anaphylaxis",
        title = "Allergic Reaction / Anaphylaxis",
        category = "Medical Emergency",
        urgency = "Immediate — can become life-threatening within minutes",
        steps = listOf(
            "Watch for severe signs: difficulty breathing, swelling of the face/lips/tongue/throat, widespread hives, dizziness, or a rapid drop in alertness — these indicate anaphylaxis, not a mild reaction.",
            "Call emergency services immediately if any severe signs are present.",
            "If the person has a known allergy and carries an epinephrine auto-injector (e.g. EpiPen), help them use it — inject into the outer thigh through clothing if needed, following the device's instructions.",
            "Have the person lie flat with legs raised unless they're having trouble breathing, in which case let them sit up in a position that's most comfortable for breathing.",
            "If symptoms don't improve within 5–10 minutes and a second auto-injector is available, it can be given.",
            "Monitor breathing and responsiveness continuously; begin CPR if they stop breathing or become unresponsive.",
            "Even if symptoms seem to improve after an epinephrine injection, the person still needs emergency medical evaluation — symptoms can return.",
        ),
        quiz = listOf(
            QuizQuestion(
                "Which signs indicate anaphylaxis rather than a mild allergic reaction?",
                listOf("A few hives on one arm", "Difficulty breathing and swelling of the face/throat", "Mild itching only", "Slight redness at a bite site"),
                1, "",
            ),
            QuizQuestion(
                "Where should an epinephrine auto-injector be administered?",
                listOf("Directly into a vein", "Into the outer thigh", "Into the arm muscle only", "Under the tongue"),
                1, "",
            ),
            QuizQuestion(
                "If symptoms improve after epinephrine, is emergency evaluation still needed?",
                listOf("No, the person is fine", "Yes — symptoms can return, so evaluation is still necessary", "Only if a second dose was given", "Only for children"),
                1, "",
            ),
        ),
    ),
)
