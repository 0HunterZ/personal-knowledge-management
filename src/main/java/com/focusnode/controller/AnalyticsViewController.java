package com.focusnode.controller;

import com.focusnode.model.DashboardMetrics;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

public class AnalyticsViewController {

    // Summary stat labels
    @FXML private Label totalFocusTimeLabel;
    @FXML private Label totalFocusTimeDiffLabel;
    @FXML private Label focusSessionsLabel;
    @FXML private Label focusSessionsDiffLabel;
    @FXML private Label taskCompletionLabel;
    @FXML private Label taskCompletionDiffLabel;
    @FXML private Label currentStreakLabel;
    @FXML private Label currentStreakDiffLabel;
    @FXML private Label focusScoreLabel;
    @FXML private Label focusScoreDiffLabel;

    // Charts
    @FXML private HBox barChartHBox;
    @FXML private VBox categoryBarsVBox;
    @FXML private FlowPane calendarFlowPane;

    // Dynamic labels (previously hardcoded)
    @FXML private Label dateRangeLabel;
    @FXML private Label calendarMonthLabel;

    // Productivity Insights
    @FXML private VBox insightsVBox;

    // Sidebar focus card
    @FXML private Label sidebarFocusTimeLabel;
    @FXML private Label sidebarFocusDiffLabel;
    @FXML private HBox sidebarMiniBarChart;

    @FXML
    public void initialize() {
        // Set date range and month labels immediately
        updateDateLabels();
        loadData();
    }

    private void updateDateLabels() {
        LocalDate now = LocalDate.now();
        // Current week: Monday to Sunday
        LocalDate monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
        DateTimeFormatter yearFmt = DateTimeFormatter.ofPattern(", yyyy");
        if (dateRangeLabel != null) {
            dateRangeLabel.setText(monday.format(fmt) + " - " + sunday.format(fmt) + sunday.format(yearFmt));
        }
        if (calendarMonthLabel != null) {
            calendarMonthLabel.setText(now.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
    }

    private void loadData() {
        com.focusnode.util.AsyncExecutor.execute(() -> {
            DashboardMetrics metrics = ServiceLocator.getAppDataService().getDashboardMetrics();
            Platform.runLater(() -> {
                updateSummary(metrics);
                updateBarChart(metrics);
                updateCategoryBars(metrics);
                updateCalendar(metrics);
                updateInsights(metrics);
                updateSidebarFocusCard(metrics);
            });
        });
    }

    private void updateSummary(DashboardMetrics metrics) {
        totalFocusTimeLabel.setText(formatMinutes(metrics.getTotalFocusMinutesThisWeek()));
        setDiff(totalFocusTimeDiffLabel, metrics.getTotalFocusMinutesThisWeek(), metrics.getTotalFocusMinutesLastWeek());

        focusSessionsLabel.setText(String.valueOf(metrics.getFocusSessionsThisWeek()));
        setDiff(focusSessionsDiffLabel, metrics.getFocusSessionsThisWeek(), metrics.getFocusSessionsLastWeek());

        taskCompletionLabel.setText(String.format("%.0f%%", metrics.getTaskCompletionRateThisWeek() * 100));
        setDiffPercent(taskCompletionDiffLabel, metrics.getTaskCompletionRateThisWeek(), metrics.getTaskCompletionRateLastWeek());

        currentStreakLabel.setText(metrics.getCurrentStreak() + " days");
        setDiff(currentStreakDiffLabel, metrics.getCurrentStreak(), metrics.getStreakLastWeek());

        focusScoreLabel.setText(String.format("%.1f", metrics.getFocusScoreThisWeek()));
        setDiffDouble(focusScoreDiffLabel, metrics.getFocusScoreThisWeek(), metrics.getFocusScoreLastWeek());
    }

    private void updateBarChart(DashboardMetrics metrics) {
        barChartHBox.getChildren().clear();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] shortDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        int maxMin = metrics.getDailyFocusMinutesThisWeek().values().stream().mapToInt(v -> v).max().orElse(1);
        if (maxMin == 0) maxMin = 1;

        for (int i = 0; i < 7; i++) {
            String day = days[i];
            int mins = metrics.getDailyFocusMinutesThisWeek().getOrDefault(day, 0);

            VBox vbox = new VBox(8);
            vbox.setAlignment(Pos.BOTTOM_CENTER);

            Label valLabel = new Label(formatMinutes(mins));
            valLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

            double height = Math.max(5, ((double) mins / maxMin) * 145);
            Rectangle rect = new Rectangle(38, height);
            rect.setArcWidth(8);
            rect.setArcHeight(8);
            rect.setFill(Color.web("#86E0C3"));
            
            Tooltip tooltip = new Tooltip(day + ": " + formatMinutes(mins));
            Tooltip.install(rect, tooltip);
            rect.setOnMouseEntered(e -> rect.setFill(Color.web("#5DD0A5")));
            rect.setOnMouseExited(e -> rect.setFill(Color.web("#86E0C3")));

            Label dayLabel = new Label(shortDays[i]);
            dayLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

            vbox.getChildren().addAll(valLabel, rect, dayLabel);
            barChartHBox.getChildren().add(vbox);
        }
    }

    private void updateCategoryBars(DashboardMetrics metrics) {
        if (categoryBarsVBox == null) return;
        categoryBarsVBox.getChildren().clear();

        Map<String, Integer> cats = metrics.getCategoryFocusMinutesThisWeek();
        int totalMins = cats.values().stream().mapToInt(v -> v).sum();

        if (totalMins == 0) {
            Label noDataLabel = new Label("No category data this week");
            noDataLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
            categoryBarsVBox.getChildren().add(noDataLabel);
            return;
        }

        String[] colors = {"#22C55E", "#60A5FA", "#A78BFA", "#FBBF24", "#F87171"};
        int cIdx = 0;

        for (Map.Entry<String, Integer> entry : cats.entrySet()) {
            if (entry.getValue() == 0) continue;
            
            String color = colors[cIdx % colors.length];
            cIdx++;

            double proportion = (double) entry.getValue() / totalMins;

            VBox row = new VBox(5);
            
            HBox labels = new HBox();
            Label nameLabel = new Label(entry.getKey());
            nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Label valueLabel = new Label(formatMinutes(entry.getValue()) + " (" + String.format("%.0f%%", proportion * 100) + ")");
            valueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
            
            labels.getChildren().addAll(nameLabel, spacer, valueLabel);

            StackPane barContainer = new StackPane();
            barContainer.setAlignment(Pos.CENTER_LEFT);
            
            Rectangle bgBar = new Rectangle(330, 8);
            bgBar.setArcWidth(8);
            bgBar.setArcHeight(8);
            bgBar.setFill(Color.web("#F1F5F9"));
            
            double fillWidth = Math.max(5, proportion * 330);
            Rectangle fillBar = new Rectangle(fillWidth, 8);
            fillBar.setArcWidth(8);
            fillBar.setArcHeight(8);
            fillBar.setFill(Color.web(color));
            
            Tooltip tooltip = new Tooltip(entry.getKey() + ": " + formatMinutes(entry.getValue()));
            Tooltip.install(fillBar, tooltip);
            
            String finalColor = color;
            fillBar.setOnMouseEntered(e -> fillBar.setFill(Color.web(finalColor).darker()));
            fillBar.setOnMouseExited(e -> fillBar.setFill(Color.web(finalColor)));

            barContainer.getChildren().addAll(bgBar, fillBar);
            
            row.getChildren().addAll(labels, barContainer);
            categoryBarsVBox.getChildren().add(row);
        }
    }

    private void updateCalendar(DashboardMetrics metrics) {
        calendarFlowPane.getChildren().clear();
        
        YearMonth currentMonth = YearMonth.now();
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate firstDay = currentMonth.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();
        
        for (int i = 1; i < startDayOfWeek; i++) {
            Label l = createCalLabel("", "#F8FAFC", "#94A3B8");
            calendarFlowPane.getChildren().add(l);
        }

        Map<String, Integer> sessions = metrics.getDailyFocusSessionsThisMonth();

        for (int d = 1; d <= daysInMonth; d++) {
            int count = sessions.getOrDefault(String.valueOf(d), 0);
            String bgColor = count > 0 ? (count >= 3 ? "#34D399" : (count == 2 ? "#6EE7B7" : "#A7F3D0")) : "#F8FAFC";
            Label l = createCalLabel(String.valueOf(d), bgColor, "#1F2937");
            
            if (count > 0) {
                Tooltip t = new Tooltip("Sessions: " + count);
                Tooltip.install(l, t);
                String finalBgColor = bgColor;
                l.setOnMouseEntered(e -> l.setStyle("-fx-alignment: center; -fx-pref-width: 66; -fx-pref-height: 36; -fx-background-color: #22C55E; -fx-background-radius: 4; -fx-text-fill: #FFFFFF;"));
                l.setOnMouseExited(e -> l.setStyle("-fx-alignment: center; -fx-pref-width: 66; -fx-pref-height: 36; -fx-background-color: " + finalBgColor + "; -fx-background-radius: 4; -fx-text-fill: #1F2937;"));
            }

            if (d == LocalDate.now().getDayOfMonth()) {
                l.setStyle(l.getStyle() + " -fx-border-color: #22C55E; -fx-border-radius: 4;");
            }

            calendarFlowPane.getChildren().add(l);
        }
    }

    private void updateInsights(DashboardMetrics metrics) {
        if (insightsVBox == null) return;
        insightsVBox.getChildren().clear();

        int avgMin = metrics.getAvgSessionMinutes();
        double morningPct = metrics.getMorningFocusPercent();
        int streak = metrics.getCurrentStreak();
        String bestDay = metrics.getBestDayOfWeek();
        int totalSessions = metrics.getTotalSessionsAllTime();

        // Insight 1: Time of day productivity
        if (morningPct > 0) {
            String timeIcon, timeTitle, timeDesc;
            if (morningPct >= 60) {
                timeIcon = "☀";
                timeTitle = "You are most productive in the morning";
                timeDesc = String.format("%.0f%% of your focus time happens before 12 PM.", morningPct);
            } else if (morningPct <= 40) {
                timeIcon = "🌙";
                timeTitle = "You are most productive in the afternoon/evening";
                timeDesc = String.format("%.0f%% of your focus time happens after 12 PM.", 100 - morningPct);
            } else {
                timeIcon = "⚖";
                timeTitle = "Balanced focus throughout the day";
                timeDesc = String.format("%.0f%% morning / %.0f%% afternoon.", morningPct, 100 - morningPct);
            }
            insightsVBox.getChildren().add(createInsightRow(timeIcon, "", timeTitle, timeDesc));
        }

        // Insight 2: Streak consistency
        if (streak > 0) {
            String streakIcon = streak >= 7 ? "🔥" : "◎";
            String streakColor = streak >= 7 ? "#F97316" : "#22C55E";
            String streakTitle = streak >= 7 ? "Amazing consistency!" : "Building a habit";
            String streakDesc = "You focused " + streak + " day" + (streak > 1 ? "s" : "") + " in a row. " + (streak >= 7 ? "Outstanding!" : "Keep going!");
            insightsVBox.getChildren().add(createInsightRow(streakIcon, streakColor, streakTitle, streakDesc));
        } else {
            insightsVBox.getChildren().add(createInsightRow("◎", "#6B7280", "Start your streak", "Focus today to begin building a daily habit."));
        }

        // Insight 3: Average session time
        if (avgMin > 0) {
            String avgTitle = "Average session: " + formatMinutes(avgMin);
            String avgDesc;
            if (avgMin >= 90) {
                avgDesc = "Deep focus sessions detected. Remember to take breaks!";
            } else if (avgMin >= 25) {
                avgDesc = "Great session length for sustained concentration.";
            } else {
                avgDesc = "Try extending your sessions for deeper focus.";
            }
            insightsVBox.getChildren().add(createInsightRow("◷", "#3B82F6", avgTitle, avgDesc));
        }

        // Insight 4: Best day / total sessions
        if (bestDay != null && !bestDay.isEmpty()) {
            insightsVBox.getChildren().add(createInsightRow("📊", "#9333EA", 
                "Best day: " + bestDay, 
                "You've completed " + totalSessions + " focus session" + (totalSessions > 1 ? "s" : "") + " total."));
        }

        // Fallback if no data at all
        if (insightsVBox.getChildren().isEmpty()) {
            insightsVBox.getChildren().add(createInsightRow("💡", "#6B7280", 
                "No data yet", "Start a focus session to see your productivity insights here."));
        }
    }

    private HBox createInsightRow(String icon, String iconColor, String title, String description) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 12;");

        Label iconLabel = new Label(icon);
        String color = (iconColor != null && !iconColor.isEmpty()) ? iconColor : "#1F2937";
        iconLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " + color + ";");

        VBox textBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        textBox.getChildren().addAll(titleLabel, descLabel);

        row.getChildren().addAll(iconLabel, textBox);
        return row;
    }

    private void updateSidebarFocusCard(DashboardMetrics metrics) {
        if (sidebarFocusTimeLabel != null) {
            sidebarFocusTimeLabel.setText(formatMinutes(metrics.getTotalFocusMinutesThisWeek()));
        }
        if (sidebarFocusDiffLabel != null) {
            setDiff(sidebarFocusDiffLabel, metrics.getTotalFocusMinutesThisWeek(), metrics.getTotalFocusMinutesLastWeek());
            sidebarFocusDiffLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + 
                (metrics.getTotalFocusMinutesThisWeek() >= metrics.getTotalFocusMinutesLastWeek() ? "#22C55E" : "#EF4444") + ";");
        }
        if (sidebarMiniBarChart != null) {
            sidebarMiniBarChart.getChildren().clear();
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            int maxMin = metrics.getDailyFocusMinutesThisWeek().values().stream().mapToInt(v -> v).max().orElse(1);
            if (maxMin == 0) maxMin = 1;

            String[] barColors = {"#D1FAE5", "#A7F3D0", "#D1FAE5", "#6EE7B7", "#A7F3D0", "#34D399", "#6EE7B7"};

            for (int i = 0; i < 7; i++) {
                int mins = metrics.getDailyFocusMinutesThisWeek().getOrDefault(days[i], 0);
                double h = Math.max(5, ((double) mins / maxMin) * 58);
                
                // Dynamically assign color by intensity
                String color;
                double ratio = (double) mins / maxMin;
                if (ratio >= 0.8) color = "#34D399";
                else if (ratio >= 0.5) color = "#6EE7B7";
                else if (ratio >= 0.2) color = "#A7F3D0";
                else color = "#D1FAE5";

                Rectangle r = new Rectangle(24, h);
                r.setArcWidth(8);
                r.setArcHeight(8);
                r.setFill(Color.web(color));

                Tooltip tooltip = new Tooltip(days[i].substring(0, 3) + ": " + formatMinutes(mins));
                Tooltip.install(r, tooltip);

                sidebarMiniBarChart.getChildren().add(r);
            }
        }
    }

    private Label createCalLabel(String text, String bgColor, String textFill) {
        Label l = new Label(text);
        l.setStyle("-fx-alignment: center; -fx-pref-width: 66; -fx-pref-height: 36; -fx-background-color: " + bgColor + "; -fx-background-radius: 4; -fx-text-fill: " + textFill + ";");
        return l;
    }

    private String formatMinutes(int totalMinutes) {
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }

    private void setDiff(Label label, int current, int past) {
        int diff = current - past;
        if (diff > 0) {
            label.setText("↑ " + diff + " vs last week");
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #22C55E;");
        } else if (diff < 0) {
            label.setText("↓ " + Math.abs(diff) + " vs last week");
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444;");
        } else {
            label.setText("— No change vs last week");
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #6B7280;");
        }
    }

    private void setDiffDouble(Label label, double current, double past) {
        double diff = current - past;
        if (diff > 0.01) {
            label.setText(String.format("↑ %.1f vs last week", diff));
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #22C55E;");
        } else if (diff < -0.01) {
            label.setText(String.format("↓ %.1f vs last week", Math.abs(diff)));
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444;");
        } else {
            label.setText("— No change vs last week");
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #6B7280;");
        }
    }

    private void setDiffPercent(Label label, double current, double past) {
        double diff = (current - past) * 100;
        if (diff > 0.1) {
            label.setText(String.format("↑ %.0f%% vs last week", diff));
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #22C55E;");
        } else if (diff < -0.1) {
            label.setText(String.format("↓ %.0f%% vs last week", Math.abs(diff)));
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444;");
        } else {
            label.setText("— No change vs last week");
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: #6B7280;");
        }
    }
}
