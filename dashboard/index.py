import streamlit as st
import pandas as pd
import numpy as np
import altair as alt
import pydeck as pdk
import os

@st.experimental_singleton
def load_data():
    data = pd.read_csv(
        "a.csv",
        names=[
            "date/time",
            "lat",
            "lon",
        ],  # specify names directly since they don't change
        skiprows=1,  # don't read header since names specified directly
        usecols=[0, 1, 2],  # doesn't load last column, constant value "B02512"
        parse_dates=[
            "date/time"
        ],  # set as datetime instead of converting after the fact
    )
    return data

def map(data, lat, lon, zoom):
    st.write(
        pdk.Deck(
            map_style="mapbox://styles/mapbox/light-v9",
            initial_view_state={
                "latitude": lat,
                "longitude": lon,
                "zoom": zoom,
                "pitch": 50,
            },
            layers=[
                pdk.Layer(
                    "HexagonLayer",
                    data=data,
                    get_position=["lon", "lat"],
                    radius=100,
                    elevation_scale=4,
                    elevation_range=[0, 1000],
                    pickable=True,
                    extruded=True,
                ),
            ],
        )
    )

@st.experimental_memo
def filterdata(df, hour_selected):
    return df[df["date/time"].dt.hour == hour_selected]


# CALCULATE MIDPOINT FOR GIVEN SET OF DATA
@st.experimental_memo
def mpoint(lat, lon):
    return (np.average(lat), np.average(lon))


# FILTER DATA BY HOUR
@st.experimental_memo
def histdata(df, hr):
    filtered = data[
        (df["date/time"].dt.hour >= hr) & (df["date/time"].dt.hour < (hr + 1))
    ]

    hist = np.histogram(filtered["date/time"].dt.minute, bins=60, range=(0, 60))[0]

    return pd.DataFrame({"minute": range(60), "pickups": hist})


st.write('# Normal Dashboard for the STF Demo')
data = load_data()

row1_1, row1_2 = st.columns((2, 3))

with row1_1:
    st.title("Invigilo Data")
    hour_selected = st.slider("Select hour of pickup", 0, 23)


# LAYING OUT THE MIDDLE SECTION OF THE APP WITH THE MAPS
row2_1, row2_2, row2_3, row2_4 = st.columns((4, 4, 4, 4))

# SETTING THE ZOOM LOCATIONS FOR THE AIRPORTS
hdec = [1.2704, 103.8015]
aedge = [1.2674, 103.6736]
changi_air = [1.3546, 103.9891]
zoom_level = 8
midpoint = mpoint(data["lat"], data["lon"])

with row2_1:
    st.write(
        f"""**CONSTRUCTION SITE 1 FROM {hour_selected}:00 and {(hour_selected + 1) % 24}:00**"""
    )
    map(filterdata(data, hour_selected), midpoint[0], midpoint[1], 11)

with row2_2:
    st.write("**SITE 2**")
    map(filterdata(data, hour_selected), hdec[0], hdec[1], zoom_level)

with row2_3:
    st.write("**SITE 3**")
    map(filterdata(data, hour_selected), aedge[0], aedge[1], zoom_level)

with row2_4:
    st.write("**SITE 4**")
    map(filterdata(data, hour_selected), changi_air[0], changi_air[1], zoom_level)

chart_data = histdata(data, hour_selected)

# LAYING OUT THE HISTOGRAM SECTION
st.write(
    f"""**Breakdown of Total Incidents per minute between range {hour_selected}:00 and {(hour_selected + 1) % 24}:00**"""
)

st.altair_chart(
    alt.Chart(chart_data)
    .mark_area(
        interpolate="step-after",
    )
    .encode(
        x=alt.X("minute:Q", scale=alt.Scale(nice=False)),
        y=alt.Y("pickups:Q"),
        tooltip=["minute", "pickups"],
    )
    .configure_mark(opacity=0.2, color="red"),
    use_container_width=True,
)